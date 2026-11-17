package statroller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/** Swing GUI that talks to the stat roller REST service. Start with: gradlew runClient */
public class StatRollerClient {

    static final String BASE_URL = "http://localhost:8090";

    private static final ObjectMapper JSON = JsonMapper.builder().findAndAddModules().build();
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final HttpClient http = HttpClient.newHttpClient();
    private final JFrame frame = new JFrame("Stat Roller");
    private final JButton rollButton = new JButton("Roll stats");
    private final JLabel scoresLabel = new JLabel(" ");
    private final JLabel diceLabel = new JLabel(" ");
    private final JLabel pointBuyLabel = new JLabel(" ");
    private final DefaultListModel<String> history = new DefaultListModel<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StatRollerClient().start());
    }

    /** "N ahead", "N behind" or "even" relative to the point-buy budget. */
    static String deltaLabel(int delta) {
        if (delta > 0) return delta + " ahead";
        if (delta < 0) return -delta + " behind";
        return "even";
    }

    /** Dice separated by spaces, with the dropped (first lowest) die struck through. */
    static String diceHtml(List<Integer> dice) {
        int dropped = dice.indexOf(Collections.min(dice));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dice.size(); i++) {
            if (i > 0) sb.append(' ');
            sb.append(i == dropped ? "<s>" + dice.get(i) + "</s>" : dice.get(i));
        }
        return sb.toString();
    }

    private static String historyRow(RollResult r) {
        String scores = r.scores().stream().map(s -> String.format("%2d", s)).collect(Collectors.joining(" "));
        return String.format("#%-3d %s   %s   %+d", r.id(), TIME.format(r.rolledAt()), scores, r.pointBuyDelta());
    }

    private void start() {
        scoresLabel.setFont(scoresLabel.getFont().deriveFont(Font.BOLD, 28f));
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        for (Component c : List.of(rollButton, scoresLabel, diceLabel, pointBuyLabel)) {
            ((javax.swing.JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(c);
            top.add(Box.createVerticalStrut(6));
        }

        JList<String> list = new JList<>(history);
        list.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createTitledBorder("History"));

        frame.add(top, BorderLayout.NORTH);
        frame.add(scroll, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(560, 460);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        rollButton.addActionListener(e -> call("POST", new TypeReference<RollResult>() {}, r -> {
            showResult(r);
            loadHistory(); // full reload, so history appears even if the startup load failed
        }));
        loadHistory();
    }

    private void loadHistory() {
        call("GET", new TypeReference<List<RollResult>>() {}, rolls -> {
            history.clear();
            rolls.forEach(r -> history.addElement(historyRow(r)));
        });
    }

    private void showResult(RollResult r) {
        scoresLabel.setText(r.scores().stream().map(String::valueOf).collect(Collectors.joining("   ")));
        diceLabel.setText("<html>" + r.dice().stream()
                .map(d -> "(" + diceHtml(d) + ")")
                .collect(Collectors.joining("&nbsp;&nbsp;")) + "</html>");
        pointBuyLabel.setText("Point buy: " + r.pointBuyCost() + " / " + StatRules.BUDGET
                + "  →  " + deltaLabel(r.pointBuyDelta()));
    }

    /** Request to /rolls; times out so a hung service can't leave the Roll button disabled forever. */
    static HttpRequest request(String method) {
        return HttpRequest.newBuilder(URI.create(BASE_URL + "/rolls"))
                .timeout(Duration.ofSeconds(5))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
    }

    /** Calls the service off the UI thread, then hands the parsed body to onSuccess on the UI thread. */
    private <T> void call(String method, TypeReference<T> type, Consumer<T> onSuccess) {
        rollButton.setEnabled(false);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                HttpResponse<String> response = http.send(request(method), HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() / 100 != 2) {
                    throw new IOException("Service answered HTTP " + response.statusCode());
                }
                return JSON.readValue(response.body(), type);
            }

            @Override
            protected void done() {
                rollButton.setEnabled(true);
                try {
                    onSuccess.accept(get());
                } catch (ExecutionException e) {
                    showError(e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void showError(Throwable cause) {
        String message = "Can't reach the stat roller service at " + BASE_URL
                + ".\nStart it first with: gradlew bootRun";
        if (!(cause instanceof ConnectException)) {
            message += "\n\n(" + cause.getMessage() + ")";
        }
        JOptionPane.showMessageDialog(frame, message, "Stat Roller", JOptionPane.ERROR_MESSAGE);
    }
}
