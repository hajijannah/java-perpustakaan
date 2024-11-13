import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class PerpustakaanGUI {
    private JFrame frame;
    private JTextArea textArea;
    private Connection connection;
    // added comment by bayu
    public PerpustakaanGUI() {
        // Koneksi ke database
        connectToDatabase();

        // GUI
        frame = new JFrame("Sistem Perpustakaan");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(64, 128, 186));
        JLabel headerLabel = new JLabel("Sistem Perpustakaan");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(headerLabel);
        frame.add(headerPanel, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(220, 220, 220));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton btnTampilkanBuku = new JButton("Tampilkan Daftar Buku");
        btnTampilkanBuku.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tampilkanDaftarBuku();
            }
        });

        JButton btnPinjamBuku = new JButton("Pinjam Buku");
        btnPinjamBuku.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pinjamBuku();
            }
        });

        JButton btnKembalikanBuku = new JButton("Kembalikan Buku");
        btnKembalikanBuku.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                kembalikanBuku();
            }
        });

        JButton btnKeluar = new JButton("Keluar");
        btnKeluar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.add(btnTampilkanBuku);
        buttonPanel.add(btnPinjamBuku);
        buttonPanel.add(btnKembalikanBuku);
        buttonPanel.add(btnKeluar);

        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void connectToDatabase() {
        try {
            // URL, username, dan password sesuaikan dengan konfigurasi MySQL Anda
            String url = "jdbc:mysql://localhost:3306/perpustakaan";
            String user = "root";
            String password = "";
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Koneksi berhasil.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Koneksi ke database gagal.");
            System.exit(1);
        }
    }

    private void tampilkanDaftarBuku() {
        textArea.setText("Daftar Buku di Perpustakaan:\n\n");
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM buku");
            while (rs.next()) {
                String status = rs.getBoolean("status_peminjaman") ? 
                                "Dipinjam oleh " + rs.getString("peminjam_nama") + " (NIM: " + rs.getString("peminjam_nim") + ")" : 
                                "Tersedia";
                textArea.append(rs.getInt("id") + ". " + rs.getString("judul") + " (" + status + ")\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void pinjamBuku() {
        String input = JOptionPane.showInputDialog(frame, "Masukkan nomor buku yang ingin dipinjam:");
        try {
            int pilihan = Integer.parseInt(input);
            String query = "SELECT * FROM buku WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, pilihan);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (!rs.getBoolean("status_peminjaman")) {
                    String nama = JOptionPane.showInputDialog(frame, "Masukkan nama peminjam:");
                    String nim = JOptionPane.showInputDialog(frame, "Masukkan NIM peminjam:");
                    String updateQuery = "UPDATE buku SET status_peminjaman = ?, peminjam_nama = ?, peminjam_nim = ? WHERE id = ?";
                    PreparedStatement updatePstmt = connection.prepareStatement(updateQuery);
                    updatePstmt.setBoolean(1, true);
                    updatePstmt.setString(2, nama);
                    updatePstmt.setString(3, nim);
                    updatePstmt.setInt(4, pilihan);
                    updatePstmt.executeUpdate();
                    JOptionPane.showMessageDialog(frame, "Anda berhasil meminjam buku: " + rs.getString("judul"));
                    tampilkanDaftarBuku();
                } else {
                    JOptionPane.showMessageDialog(frame, "Maaf, buku tersebut sudah dipinjam.");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Nomor buku tidak valid.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Input tidak valid.");
        }
    }

    private void kembalikanBuku() {
        String input = JOptionPane.showInputDialog(frame, "Masukkan nomor buku yang ingin dikembalikan:");
        try {
            int kembali = Integer.parseInt(input);
            String query = "SELECT * FROM buku WHERE id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, kembali);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getBoolean("status_peminjaman")) {
                    String updateQuery = "UPDATE buku SET status_peminjaman = ?, peminjam_nama = NULL, peminjam_nim = NULL WHERE id = ?";
                    PreparedStatement updatePstmt = connection.prepareStatement(updateQuery);
                    updatePstmt.setBoolean(1, false);
                    updatePstmt.setInt(2, kembali);
                    updatePstmt.executeUpdate();
                    JOptionPane.showMessageDialog(frame, "Buku " + rs.getString("judul") + " berhasil dikembalikan.");
                    tampilkanDaftarBuku();
                } else {
                    JOptionPane.showMessageDialog(frame, "Buku tersebut belum dipinjam.");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Nomor buku tidak valid.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Input tidak valid.");
        }
    }

    public static void main(String[] args) {
        new PerpustakaanGUI();
    }
}
