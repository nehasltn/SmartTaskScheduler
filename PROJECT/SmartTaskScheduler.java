import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

// --------- Task Class ----------
class Task implements Comparable<Task> {
    String title;
    int priority; // lower = higher priority
    LocalDateTime deadline;
    boolean completed = false;

    Task(String title, int priority, LocalDateTime deadline) {
        this.title = title;
        this.priority = priority;
        this.deadline = deadline;
    }

    @Override
    public int compareTo(Task o) {
        if (this.completed != o.completed) return this.completed ? 1 : -1;
        if (this.deadline != null && o.deadline != null) {
            return this.deadline.compareTo(o.deadline);
        }
        return Integer.compare(this.priority, o.priority);
    }
}

// --------- Task Manager ----------
class TaskManager {
    private final PriorityQueue<Task> tasks = new PriorityQueue<>();

    void add(Task t) { tasks.add(t); }
    void remove(Task t) { tasks.remove(t); }
    PriorityQueue<Task> getTasks() { return tasks; }
}

// --------- Reminder Service ----------
class ReminderService {
    private final Timer timer = new Timer("reminders", true);
    private final TaskManager manager;

    ReminderService(TaskManager manager) { this.manager = manager; }

    void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Task t : manager.getTasks()) {
                    if (!t.completed && t.deadline != null &&
                        t.deadline.minusMinutes(5).isBefore(LocalDateTime.now())) {
                        System.out.println("⏰ Reminder: Task \"" + t.title + "\" is due soon!");
                    }
                }
            }
        }, 0, 60_000); // check every minute
    }
}

// --------- Main App (Swing UI) ----------
public class SmartTaskScheduler extends JFrame {
    private final TaskManager manager = new TaskManager();
    private final DefaultTableModel model;

    public SmartTaskScheduler() {
        setTitle("Smart Task Scheduler");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        model = new DefaultTableModel(new String[]{"Title", "Priority", "Deadline", "Status"}, 0);
        JTable table = new JTable(model);

        JButton addBtn = new JButton("Add Task");
        JButton delBtn = new JButton("Delete Task");
        JButton doneBtn = new JButton("Mark Done");

        addBtn.addActionListener(e -> addTask());
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                manager.getTasks().removeIf(t -> t.title.equals(model.getValueAt(row, 0)));
                model.removeRow(row);
            }
        });
        doneBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                String title = (String) model.getValueAt(row, 0);
                for (Task t : manager.getTasks()) {
                    if (t.title.equals(title)) {
                        t.completed = true;
                        model.setValueAt("Done", row, 3);
                    }
                }
            }
        });

        JPanel buttons = new JPanel();
        buttons.add(addBtn);
        buttons.add(delBtn);
        buttons.add(doneBtn);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        new ReminderService(manager).start();
    }

    private void addTask() {
        String title = JOptionPane.showInputDialog("Enter task title:");
        int priority = Integer.parseInt(JOptionPane.showInputDialog("Enter priority (1=High):"));
        String deadlineStr = JOptionPane.showInputDialog("Enter deadline (yyyy-MM-dd HH:mm):");

        LocalDateTime deadline = null;
        try {
            deadline = LocalDateTime.parse(deadlineStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception ignored) {}

        Task t = new Task(title, priority, deadline);
        manager.add(t);
        model.addRow(new Object[]{
                t.title,
                t.priority,
                deadline != null ? deadline.toString() : "None",
                t.completed ? "Done" : "Pending"
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SmartTaskScheduler().setVisible(true));
    }
}