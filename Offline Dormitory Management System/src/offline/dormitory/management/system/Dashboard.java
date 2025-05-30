package offline.dormitory.management.system;

import javax.swing.JOptionPane;
import com.google.gson.Gson;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import javax.swing.table.DefaultTableModel;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;

public class Dashboard extends javax.swing.JFrame {
    
private void syncToPaymentTable() {
DefaultTableModel studentModel = (DefaultTableModel) studentTable.getModel();
DefaultTableModel bookedModel = (DefaultTableModel) BookedTable.getModel();
DefaultTableModel availableModel = (DefaultTableModel) AvailableTable.getModel();
DefaultTableModel unavailableModel = (DefaultTableModel) UnavailableTable.getModel();

bookedModel.setRowCount(0);
unavailableModel.setRowCount(0);

java.util.Map<String, Integer> roomOccupantsCount = new java.util.HashMap<>();

for (int i = 0; i < studentModel.getRowCount(); i++) {
    String status = studentModel.getValueAt(i, 8).toString(); 
    String assignedRoom = studentModel.getValueAt(i, 7).toString(); 
    String studentName = studentModel.getValueAt(i, 0).toString(); 
    String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

    roomOccupantsCount.put(assignedRoom, roomOccupantsCount.getOrDefault(assignedRoom, 0) + 1);

    if ("Moved In".equalsIgnoreCase(status)) {
        unavailableModel.addRow(new Object[]{assignedRoom, studentName, today});
    } else {
        bookedModel.addRow(new Object[]{assignedRoom, studentName, today});
    }
}

availableModel.setRowCount(0); 

for (int i = 1; i <= 10; i++) {
    String room = "Room " + i;
    int occupants = roomOccupantsCount.getOrDefault(room, 0);
    int slotsLeft = 4 - occupants; 
    if (slotsLeft > 0) {
        availableModel.addRow(new Object[]{room, slotsLeft});
    } else {
    }
}

}


    private void moveToUnavailableTable() {
    DefaultTableModel bookedModel = (DefaultTableModel) BookedTable.getModel();
DefaultTableModel unavailableModel = (DefaultTableModel) UnavailableTable.getModel();
DefaultTableModel studentModel = (DefaultTableModel) studentTable.getModel();

int rowCount = bookedModel.getRowCount();
for (int i = rowCount - 1; i >= 0; i--) { 
    String room = bookedModel.getValueAt(i, 0).toString();
    String student = bookedModel.getValueAt(i, 1).toString(); 
    Object moveDate = bookedModel.getValueAt(i, 2); 
    
    if (moveDate == null || moveDate.toString().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter move-in date for " + student + " in Room " + room);
        continue;
    }

    unavailableModel.addRow(new Object[]{room, student, moveDate});
    
    bookedModel.removeRow(i);

    for (int j = 0; j < studentModel.getRowCount(); j++) {
        String studentNameInStudentTable = studentModel.getValueAt(j, 0).toString(); 
        if (student.equals(studentNameInStudentTable)) {
            studentModel.setValueAt("Moved In", j, 8); 
            break;
        }
    }
}

}
    private void updateRoomComboBox() {
    room1.removeAllItems(); 

    DefaultTableModel bookedModel = (DefaultTableModel) BookedTable.getModel();
    java.util.Map<String, Integer> roomCount = new java.util.HashMap<>();

    for (int i = 0; i < bookedModel.getRowCount(); i++) {
        String room = bookedModel.getValueAt(i, 0).toString();
        roomCount.put(room, roomCount.getOrDefault(room, 0) + 1);
    }
    for (int i = 1; i <= 10; i++) {
        String roomName = "Room " + i;
        int occupants = roomCount.getOrDefault(roomName, 0);

        if (occupants < 4) {
            room1.addItem(roomName);
        }
    }
}
    private void loadFromFile() {
    File file = new File("students.txt");
    if (!file.exists()) {
        return; 
    }
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
        model.setRowCount(0); 

        String line;
        while ((line = br.readLine()) != null) {
            String[] data = line.split("\\|");
            if (data.length == model.getColumnCount()) {
                model.addRow(data);
            }
        }
    } catch (IOException ex) {
        JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage());
        }
    }
    private void saveToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("students.txt"))) {
            DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < model.getColumnCount(); j++) {
                    sb.append(model.getValueAt(i, j));
                    if (j < model.getColumnCount() - 1) sb.append("|");
                }
                bw.write(sb.toString());
                bw.newLine();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
        }
    }
    private void clearFields() {
        s_name.setText("");
        s_bday.setText("");
        s_id.setText("");
        course.setSelectedIndex(0);
        gender.setSelectedIndex(0);
        phoneno.setText("");
        address.setText("");
        room1.setSelectedIndex(0);
}
    private void resetFields() {
        depositField.setText("");
        downPaymentField.setText("");
        costField.setText("");
        electricityField.setText("");
        taxField.setText("");
        waterField.setText("");
        totalPaymentField.setText("0.00");
        receiptArea.setText("");
    }
    public Dashboard() {
        
        initComponents();
        loadFromFile();
        
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                syncToPaymentTable();
            }
        });
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetFields();
            }
        });
        btnMoveBackToBooked.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            DefaultTableModel unavailableModel = (DefaultTableModel) UnavailableTable.getModel();
            DefaultTableModel bookedModel = (DefaultTableModel) BookedTable.getModel();
            DefaultTableModel studentModel = (DefaultTableModel) studentTable.getModel();

            int selectedRow = UnavailableTable.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a student to move back to booked rooms.");
                return;
            }
            String roomNo = unavailableModel.getValueAt(selectedRow, 0).toString();
            String studentName = unavailableModel.getValueAt(selectedRow, 1).toString();
            String today = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());

            bookedModel.addRow(new Object[]{roomNo, studentName, today});
            unavailableModel.removeRow(selectedRow);
            updateRoomComboBox();
            JOptionPane.showMessageDialog(null, studentName + " Has been moved back to Booked Rooms.");
            studentModel.setValueAt("Pending", selectedRow, 8); 
            syncToPaymentTable();
            saveToFile(); 
            
        }
    });
               
        KeyAdapter recalculateListener = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            calculateTotalPayment();
        }

        private void calculateTotalPayment() {
            double total = 0.0;
            DefaultTableModel model = (DefaultTableModel) studentTable.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {
            Object isPaidObj = model.getValueAt(i, 8); 
            Object amountObj = model.getValueAt(i, 9); 

        if (isPaidObj instanceof Boolean && (Boolean) isPaidObj) {
            try {
                double amount = Double.parseDouble(amountObj.toString());
                total += amount;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid payment value in row " + i);
                }
            }
        }
        totalPaymentField.setText(String.format("%.2f", total)); 
    }
};

        costField.addKeyListener(recalculateListener);
        electricityField.addKeyListener(recalculateListener);
        taxField.addKeyListener(recalculateListener);
        waterField.addKeyListener(recalculateListener);

        totalPaymentField.setEditable(false);
        System.out.println("studentTable is " + studentTable);
        loadFromFile();
        syncToPaymentTable();
        updateRoomComboBox();
    
        BookedTable.setModel(new DefaultTableModel(
            new Object[][]{},
            new String[]{"Room No.", "Student Name", "Date to move"} 
            ) {
                
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2 || column == 3; 
            }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 3 ? Boolean.class : String.class; 
        }
    });

    
        UnavailableTable.setModel(new DefaultTableModel(
           new Object[][]{},
           new String[]{"Room No.", "Student Name", "Date moved", "Payment Status"}
       ) {
           @Override
           public boolean isCellEditable(int row, int column) {
               return column == 2 || column == 3;
           }

           @Override
           public Class<?> getColumnClass(int columnIndex) {
               if (columnIndex == 3) return Boolean.class; 
               return String.class;
           }
       });  

}
        private boolean validateInputs() {
        if (s_name.getText().trim().isEmpty()) {
            errorLabel.setText("Name is required!");
            s_name.requestFocus();
            return false;
        }
        if (s_bday.getText().trim().isEmpty()) {
            errorLabel.setText("Birth Date is required!");
            s_bday.requestFocus();
            return false;
        }
        if (s_id.getText().trim().isEmpty()) {
            errorLabel.setText("Student ID is required!");
            s_id.requestFocus();
            return false;
        }
        if (phoneno.getText().trim().isEmpty()) {
            errorLabel.setText("Phone number is required!");
            phoneno.requestFocus();
            return false;
        }
        if (address.getText().trim().isEmpty()) {
            errorLabel.setText("Address is required!");
            address.requestFocus();
            return false;
        }

        errorLabel.setText(""); 
        return true;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton9 = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        s_name = new javax.swing.JTextField();
        s_bday = new javax.swing.JTextField();
        s_id = new javax.swing.JTextField();
        gender = new javax.swing.JComboBox<>();
        phoneno = new javax.swing.JTextField();
        address = new javax.swing.JTextField();
        room1 = new javax.swing.JComboBox<>();
        add = new javax.swing.JButton();
        clear = new javax.swing.JButton();
        errorLabel = new javax.swing.JLabel();
        course = new javax.swing.JComboBox<>();
        jPanel5 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        update = new javax.swing.JButton();
        delete = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel31 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        studentTable = new javax.swing.JTable();
        jTextField1 = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        BookedTable = new javax.swing.JTable();
        jLabel12 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        AvailableTable = new javax.swing.JTable();
        jLabel13 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        UnavailableTable = new javax.swing.JTable();
        jLabel14 = new javax.swing.JLabel();
        confirmMoveInButton = new javax.swing.JButton();
        btnMoveBackToBooked = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jPanel14 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        firstnameField = new javax.swing.JTextField();
        addressField = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        studentIDField = new javax.swing.JTextField();
        surnameField = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        depositField = new javax.swing.JTextField();
        downPaymentField = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jPanel20 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        costField = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        roomComboBox = new javax.swing.JTextField();
        jPanel16 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        electricityField = new javax.swing.JTextField();
        taxField = new javax.swing.JTextField();
        waterField = new javax.swing.JTextField();
        totalPaymentField = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        receiptArea = new javax.swing.JTextArea();
        jPanel18 = new javax.swing.JPanel();
        rentalButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(0, 204, 204));

        jPanel3.setBackground(new java.awt.Color(0, 204, 204));

        jPanel4.setBackground(new java.awt.Color(0, 102, 102));
        jPanel4.setForeground(new java.awt.Color(255, 255, 255));
        jPanel4.setFont(new java.awt.Font("Akira Expanded", 0, 24)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Akira Expanded", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("D O R M I T O R Y   M A N A G E M E N T   S Y S T E M");

        jButton9.setBackground(new java.awt.Color(204, 0, 0));
        jButton9.setFont(new java.awt.Font("Times New Roman", 1, 20)); // NOI18N
        jButton9.setForeground(new java.awt.Color(255, 255, 255));
        jButton9.setText("Log Out");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jButton9))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.setBackground(new java.awt.Color(0, 204, 204));
        tabbedPane.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 204), 4, true));
        tabbedPane.setFont(new java.awt.Font("Times New Roman", 1, 20)); // NOI18N
        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });
        tabbedPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabbedPaneMouseClicked(evt);
            }
        });
        tabbedPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                tabbedPaneComponentShown(evt);
            }
        });
        tabbedPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tabbedPaneKeyPressed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(0, 51, 51));
        jPanel1.setPreferredSize(new java.awt.Dimension(1082, 640));

        jPanel2.setBackground(new java.awt.Color(0, 102, 102));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 204), 4, true));

        jLabel2.setFont(new java.awt.Font("Akira Expanded", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("A d d  N e w  S t u d e n t");

        jLabel3.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Student's Name");

        jLabel4.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Date Of Birth");

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Student's I'D");

        jLabel6.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Course");

        jLabel7.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Gender");

        jLabel8.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Phone Number");

        jLabel9.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Address");

        jLabel10.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("Room Number");

        s_name.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        s_name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                s_nameActionPerformed(evt);
            }
        });

        s_bday.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N

        s_id.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N

        gender.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        gender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male ", "Female" }));

        phoneno.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        phoneno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phonenoActionPerformed(evt);
            }
        });
        phoneno.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                phonenoKeyTyped(evt);
            }
        });

        address.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N

        room1.setFont(new java.awt.Font("Times New Roman", 0, 16)); // NOI18N
        room1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Room 1", "Room 2", "Room 3", "Room 4", "Room 5", "Room 6", "Room 7", "Room 8", "Room 9", "Room 10" }));

        add.setBackground(new java.awt.Color(0, 153, 51));
        add.setFont(new java.awt.Font("Times New Roman", 1, 20)); // NOI18N
        add.setForeground(new java.awt.Color(255, 255, 255));
        add.setText("Add");
        add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addActionPerformed(evt);
            }
        });

        clear.setBackground(new java.awt.Color(204, 0, 0));
        clear.setFont(new java.awt.Font("Times New Roman", 1, 20)); // NOI18N
        clear.setForeground(new java.awt.Color(255, 255, 255));
        clear.setText("Clear");
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActionPerformed(evt);
            }
        });

        errorLabel.setForeground(Color.RED);
        errorLabel.setOpaque(true);  // important to show background color
        errorLabel.setBackground(Color.WHITE);
        errorLabel.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        errorLabel.setText("");

        course.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        course.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "School of Forestry and Environmental Science ", "School of Education ", "School of Agriculture Sciences ", "School of Engineering ", "School of Aquatic Sciences ", "School of Arts and Sciences ", "School of Industrial Technology ", "School of Information Technology ", "College of Law" }));
        course.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                courseActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(clear, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)
                                .addComponent(add, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel10))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(s_name)
                                    .addComponent(s_bday)
                                    .addComponent(s_id)
                                    .addComponent(address)
                                    .addComponent(gender, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(phoneno)
                                    .addComponent(room1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(course, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(9, 9, 9)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(165, 165, 165)
                .addComponent(errorLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(jLabel2)
                .addGap(47, 47, 47)
                .addComponent(errorLabel)
                .addGap(46, 46, 46)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(s_name, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(s_bday, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(s_id, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(course, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gender, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(phoneno, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(room1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addGap(48, 48, 48)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clear)
                    .addComponent(add))
                .addContainerGap())
        );

        jPanel5.setBackground(new java.awt.Color(0, 153, 153));
        jPanel5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 204), 4, true));

        jPanel7.setBackground(new java.awt.Color(0, 102, 102));

        jLabel11.setFont(new java.awt.Font("Akira Expanded", 0, 18)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("S t u d e n t   L i s t ");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 338, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addContainerGap())
        );

        update.setBackground(new java.awt.Color(0, 0, 255));
        update.setFont(new java.awt.Font("Times New Roman", 1, 20)); // NOI18N
        update.setForeground(new java.awt.Color(255, 255, 255));
        update.setText("Update");
        update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateActionPerformed(evt);
            }
        });

        delete.setBackground(new java.awt.Color(204, 0, 0));
        delete.setFont(new java.awt.Font("Times New Roman", 1, 20)); // NOI18N
        delete.setForeground(new java.awt.Color(255, 255, 255));
        delete.setText("Delete");
        delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteActionPerformed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(0, 102, 0));
        jButton1.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Billing");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel31.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(255, 255, 255));
        jLabel31.setText("!! Select a student first before pressing a button !!");

        studentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Student Name", "Birth Date", "ID", "Course", "Gender", "Phone Number", "Address", "Room Number", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(studentTable);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel31)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 253, Short.MAX_VALUE)
                        .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(update, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 404, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(delete, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(update, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1)
                    .addComponent(jLabel31))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        jLabel32.setFont(new java.awt.Font("Berlin Sans FB Demi", 0, 18)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(255, 255, 255));
        jLabel32.setText("Search Data");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField1)
                            .addComponent(jLabel32))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        tabbedPane.addTab("Student Management", jPanel1);

        jPanel8.setBackground(new java.awt.Color(0, 51, 51));

        jPanel10.setBackground(new java.awt.Color(0, 153, 153));
        jPanel10.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 204), 4, true));

        jPanel11.setBackground(new java.awt.Color(0, 51, 51));
        jPanel11.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jPanel11FocusGained(evt);
            }
        });

        jPanel9.setBackground(new java.awt.Color(0, 102, 102));

        BookedTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Room No.", "Student's Name", "Date to move"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        BookedTable.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                BookedTableFocusGained(evt);
            }
        });
        jScrollPane2.setViewportView(BookedTable);

        jLabel12.setBackground(new java.awt.Color(255, 255, 153));
        jLabel12.setFont(new java.awt.Font("Akira Expanded", 0, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("B o o k e d  R o o m s");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel12.setBackground(new java.awt.Color(0, 102, 102));

        AvailableTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Room No.", "Slots"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        AvailableTable.setToolTipText("");
        jScrollPane4.setViewportView(AvailableTable);

        jLabel13.setBackground(new java.awt.Color(255, 255, 153));
        jLabel13.setFont(new java.awt.Font("Akira Expanded", 0, 18)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("A v a i l a b l e  R o o m s");

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel13)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel13.setBackground(new java.awt.Color(0, 102, 102));

        UnavailableTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Room No.", "Student's Name", "Date moved", "Payment Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(UnavailableTable);

        jLabel14.setBackground(new java.awt.Color(255, 255, 153));
        jLabel14.setFont(new java.awt.Font("Akira Expanded", 0, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("U n a v a i l a b l e  R o o m s");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                .addContainerGap())
        );

        confirmMoveInButton.setBackground(new java.awt.Color(0, 102, 51));
        confirmMoveInButton.setFont(new java.awt.Font("Times New Roman", 1, 20)); // NOI18N
        confirmMoveInButton.setForeground(new java.awt.Color(255, 255, 255));
        confirmMoveInButton.setText("Move");
        confirmMoveInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confirmMoveInButtonActionPerformed(evt);
            }
        });

        btnMoveBackToBooked.setBackground(new java.awt.Color(0, 0, 102));
        btnMoveBackToBooked.setFont(new java.awt.Font("Times New Roman", 1, 20)); // NOI18N
        btnMoveBackToBooked.setForeground(new java.awt.Color(255, 255, 255));
        btnMoveBackToBooked.setText("Back");
        btnMoveBackToBooked.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveBackToBookedActionPerformed(evt);
            }
        });

        jLabel15.setIcon(new javax.swing.ImageIcon("C:\\Users\\MARLYN\\Documents\\NetBeansProjects\\Offline Dormitory Management System\\src\\icon\\icon.png")); // NOI18N

        jLabel29.setFont(new java.awt.Font("Berlin Sans FB Demi", 0, 24)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(255, 255, 255));
        jLabel29.setText("Dormitory Management");

        jLabel30.setFont(new java.awt.Font("Berlin Sans FB Demi", 0, 24)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(255, 255, 255));
        jLabel30.setText("System");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap(181, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel29)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel15))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(85, 85, 85)
                        .addComponent(jLabel30)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 182, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                        .addComponent(btnMoveBackToBooked, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(confirmMoveInButton, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(109, 109, 109)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel30)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(confirmMoveInButton)
                    .addComponent(btnMoveBackToBooked))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.addTab("Manage Room & Payment", jPanel8);

        jPanel14.setBackground(new java.awt.Color(0, 51, 51));

        jPanel15.setBackground(new java.awt.Color(0, 102, 102));
        jPanel15.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 204), 4, true));
        jPanel15.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N

        jLabel16.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("Student ID");

        firstnameField.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        firstnameField.setEnabled(false);
        firstnameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firstnameFieldActionPerformed(evt);
            }
        });

        addressField.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        addressField.setEnabled(false);
        addressField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressFieldActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setText("Firstname");

        jLabel20.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setText("Surename");

        studentIDField.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        studentIDField.setEnabled(false);
        studentIDField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                studentIDFieldActionPerformed(evt);
            }
        });

        surnameField.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        surnameField.setEnabled(false);
        surnameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                surnameFieldActionPerformed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("Down Payment");

        jLabel21.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setText("Deposit");

        depositField.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        depositField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                depositFieldActionPerformed(evt);
            }
        });
        depositField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                depositFieldKeyTyped(evt);
            }
        });

        downPaymentField.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        downPaymentField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downPaymentFieldActionPerformed(evt);
            }
        });
        downPaymentField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                downPaymentFieldKeyTyped(evt);
            }
        });

        jLabel22.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setText("Address");

        jLabel37.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(255, 255, 255));
        jLabel37.setText("Student Details");

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel15Layout.createSequentialGroup()
                                .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(surnameField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                                .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(firstnameField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(studentIDField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(depositField, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(downPaymentField, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(90, 90, 90))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jLabel37)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel15Layout.createSequentialGroup()
                    .addGap(16, 16, 16)
                    .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(studentIDField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel22))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                        .addComponent(addressField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(firstnameField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(depositField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(surnameField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(downPaymentField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
            .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                    .addContainerGap(81, Short.MAX_VALUE)
                    .addComponent(jLabel18)
                    .addGap(104, 104, 104)))
        );

        jPanel20.setBackground(new java.awt.Color(0, 102, 102));
        jPanel20.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 204), 4, true));

        jLabel23.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setText("Number of Room");

        jLabel24.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("Cost");

        costField.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        costField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                costFieldKeyTyped(evt);
            }
        });

        jLabel38.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(255, 255, 255));
        jLabel38.setText("Rental Details");

        roomComboBox.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        roomComboBox.setEnabled(false);
        roomComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                roomComboBoxActionPerformed(evt);
            }
        });
        roomComboBox.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                roomComboBoxKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel20Layout.createSequentialGroup()
                                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(roomComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel20Layout.createSequentialGroup()
                                .addComponent(jLabel24)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                                .addComponent(costField, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(60, 60, 60))
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(jLabel38)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(roomComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(costField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addContainerGap(165, Short.MAX_VALUE))
        );

        jPanel16.setBackground(new java.awt.Color(0, 102, 102));
        jPanel16.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 204), 4, true));

        jLabel25.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setText("Electricity");

        jLabel26.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setText("Local Tax");

        jLabel27.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 255, 255));
        jLabel27.setText("Water Bill");

        jLabel28.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setText("Total payment");

        electricityField.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        electricityField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                electricityFieldKeyTyped(evt);
            }
        });

        taxField.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        taxField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                taxFieldKeyTyped(evt);
            }
        });

        waterField.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        waterField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                waterFieldActionPerformed(evt);
            }
        });
        waterField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                waterFieldKeyTyped(evt);
            }
        });

        totalPaymentField.setFont(new java.awt.Font("Times New Roman", 0, 14)); // NOI18N
        totalPaymentField.setEnabled(false);
        totalPaymentField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                totalPaymentFieldActionPerformed(evt);
            }
        });

        jLabel39.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(255, 255, 255));
        jLabel39.setText("Utility Bills");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(33, 33, 33)
                        .addComponent(electricityField))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel28)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(totalPaymentField))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addGap(35, 35, 35)
                        .addComponent(taxField))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addGap(33, 33, 33)
                        .addComponent(waterField))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel39)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(42, 42, 42)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(electricityField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(taxField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel26))
                .addGap(18, 18, 18)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(waterField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalPaymentField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel28))
                .addContainerGap(72, Short.MAX_VALUE))
        );

        jPanel17.setBackground(new java.awt.Color(0, 102, 102));
        jPanel17.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 204), 4, true));

        receiptArea.setColumns(20);
        receiptArea.setFont(new java.awt.Font("Monospaced", 0, 16)); // NOI18N
        receiptArea.setRows(5);
        jScrollPane3.setViewportView(receiptArea);

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3)
                .addContainerGap())
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3)
                .addContainerGap())
        );

        jPanel18.setBackground(new java.awt.Color(0, 102, 102));
        jPanel18.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 204, 204), 4, true));

        rentalButton.setBackground(new java.awt.Color(0, 102, 51));
        rentalButton.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        rentalButton.setForeground(new java.awt.Color(255, 255, 255));
        rentalButton.setText("Rental");
        rentalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rentalButtonActionPerformed(evt);
            }
        });

        resetButton.setBackground(new java.awt.Color(153, 0, 0));
        resetButton.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        resetButton.setForeground(new java.awt.Color(255, 255, 255));
        resetButton.setText("Reset");

        jButton4.setBackground(new java.awt.Color(0, 0, 102));
        jButton4.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("Print");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(rentalButton)
                .addGap(18, 18, 18)
                .addComponent(jButton4)
                .addGap(18, 18, 18)
                .addComponent(resetButton)
                .addContainerGap(284, Short.MAX_VALUE))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel18Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(rentalButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(resetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        tabbedPane.addTab("Receipt", jPanel14);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tabbedPane))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 584, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        int a = JOptionPane.showConfirmDialog(this, "Do you want to log out now?", "Select",JOptionPane.YES_NO_OPTION);
        if(a == 0){
            this.dispose();
        }
        new LoginForm().setVisible(true);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        Document document = new Document();
        try {
            BaseFont bf = BaseFont.createFont(BaseFont.COURIER, BaseFont.CP1252, BaseFont.EMBEDDED);
            Font monoFont = new Font(bf, 12, Font.NORMAL);

            String folderPath = System.getProperty("user.home") + "/Documents/Receipts";
            File folder = new File(folderPath);
            if (!folder.exists() && !folder.mkdirs()) {
                JOptionPane.showMessageDialog(this, "Failed to create folder: " + folderPath);
                return;
            }

            String fileName = folderPath + "/Receipt_" + System.currentTimeMillis() + ".pdf";

            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();
            document.add(new Paragraph(receiptArea.getText(), monoFont));
            document.close();

            JOptionPane.showMessageDialog(this, "PDF saved at: " + fileName);
        } catch (DocumentException | IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to export PDF: " + e.getMessage());
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void rentalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rentalButtonActionPerformed
        try {
            double cost = Double.parseDouble(costField.getText());
            double electricity = Double.parseDouble(electricityField.getText());
            double tax = Double.parseDouble(taxField.getText());
            double water = Double.parseDouble(waterField.getText());

            double total = cost + electricity + tax + water;
            totalPaymentField.setText(String.format("%.2f", total));
        } catch (NumberFormatException e) {
            totalPaymentField.setText("0.00");
        }
        String studentID = studentIDField.getText();
        String firstname = firstnameField.getText();
        String surname = surnameField.getText();
        String adresss = addressField.getText();
        String deposit = depositField.getText();
        String downPayment = downPaymentField.getText();

        String room = roomComboBox.getText();
        String cost = costField.getText();

        String electricity = electricityField.getText();
        String tax = taxField.getText();
        String water = waterField.getText();
        String total = totalPaymentField.getText();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now.plusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateTimeString = now.format(formatter);
        String nextdateTimeString = next.format(formatter);
        System.out.println(dateTimeString);

        StringBuilder receipt = new StringBuilder();
        receipt.append("     ======================================================\n");
        receipt.append("     ========   AURORA STATE COLLEGE OF TECHNOLOGY  =======\n");
        receipt.append("     ================= DORMITORY RECEIPT ==================\n");
        receipt.append("     ======================================================\n");
        receipt.append(String.format("     %-18s: %s\n", "Student ID", studentID));
        receipt.append(String.format("     %-18s: %s\n", "Firstname", firstname));
        receipt.append(String.format("     %-18s: %s\n", "Surname", surname));
        receipt.append(String.format("     %-18s: %s\n", "Address", adresss));
        receipt.append(String.format("     %-18s: ₱%s\n", "Deposit", deposit));
        receipt.append(String.format("     %-18s: ₱%s\n", "Down Payment", downPayment));
        receipt.append("\n");
        receipt.append(String.format("     %-18s: %s\n", "Room Number", room));
        receipt.append(String.format("     %-18s: ₱%s\n", "Room Cost", cost));
        receipt.append("\n");
        receipt.append(String.format("     %-18s: ₱%s\n", "Electricity", electricity));
        receipt.append(String.format("     %-18s: ₱%s\n", "Local Tax", tax));
        receipt.append(String.format("     %-18s: ₱%s\n", "Water Bill", water));
        receipt.append("     -------------------------------------------------------\n");
        receipt.append(String.format("     %-18s: ₱%s\n", "TOTAL PAYMENT", total));
        receipt.append("     -------------------------------------------------------\n");
        receipt.append(String.format("     Date Recieved: " + dateTimeString));
        receipt.append("\n");
        receipt.append("     -------------------------------------------------------\n");
        receipt.append(String.format("     Valid Until:   " + nextdateTimeString));
        receipt.append("\n");
        receipt.append("     =======================================================\n");

        receiptArea.setText(receipt.toString());
    }//GEN-LAST:event_rentalButtonActionPerformed

    private void totalPaymentFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_totalPaymentFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_totalPaymentFieldActionPerformed

    private void waterFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_waterFieldKeyTyped
        if(!Character.isDigit(evt.getKeyChar())){
            evt.consume();
        }
    }//GEN-LAST:event_waterFieldKeyTyped

    private void waterFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_waterFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_waterFieldActionPerformed

    private void taxFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_taxFieldKeyTyped
        if(!Character.isDigit(evt.getKeyChar())){
            evt.consume();
        }
    }//GEN-LAST:event_taxFieldKeyTyped

    private void electricityFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_electricityFieldKeyTyped
        if(!Character.isDigit(evt.getKeyChar())){
            evt.consume();
        }
    }//GEN-LAST:event_electricityFieldKeyTyped

    private void costFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_costFieldKeyTyped
        if(!Character.isDigit(evt.getKeyChar())){
            evt.consume();
        }
    }//GEN-LAST:event_costFieldKeyTyped

    private void downPaymentFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_downPaymentFieldKeyTyped
        if(!Character.isDigit(evt.getKeyChar())){
            evt.consume();
        }
    }//GEN-LAST:event_downPaymentFieldKeyTyped

    private void downPaymentFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downPaymentFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_downPaymentFieldActionPerformed

    private void depositFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_depositFieldKeyTyped
        if(!Character.isDigit(evt.getKeyChar())){
            evt.consume();
        }
    }//GEN-LAST:event_depositFieldKeyTyped

    private void depositFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_depositFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_depositFieldActionPerformed

    private void surnameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_surnameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_surnameFieldActionPerformed

    private void studentIDFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_studentIDFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_studentIDFieldActionPerformed

    private void addressFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addressFieldActionPerformed

    private void firstnameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstnameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_firstnameFieldActionPerformed

    private void btnMoveBackToBookedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveBackToBookedActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnMoveBackToBookedActionPerformed

    private void confirmMoveInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confirmMoveInButtonActionPerformed
int selectedRow = BookedTable.getSelectedRow(); 
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a student to confirm move-in.");
        return;
    }

    DefaultTableModel bookedModel = (DefaultTableModel) BookedTable.getModel();
    DefaultTableModel unavailableModel = (DefaultTableModel) UnavailableTable.getModel();
    DefaultTableModel studentModel = (DefaultTableModel) studentTable.getModel();

    String roomNo = bookedModel.getValueAt(selectedRow, 0).toString();
    String studentName = bookedModel.getValueAt(selectedRow, 1).toString();
    String dateToMove = bookedModel.getValueAt(selectedRow, 2).toString();

    unavailableModel.addRow(new Object[]{roomNo, studentName, dateToMove, true});

    bookedModel.removeRow(selectedRow);

    for (int i = 0; i < studentModel.getRowCount(); i++) {
        String name = studentModel.getValueAt(i, 0).toString();
        String room = studentModel.getValueAt(i, 7).toString();
        if (name.equals(studentName) && room.equals(roomNo)) {
            studentModel.setValueAt("Moved In", i, 8); 
            break;
        }
    }

    updateRoomComboBox();
    JOptionPane.showMessageDialog(this, "Move-in confirmed for " + studentName);
saveToFile();
    }//GEN-LAST:event_confirmMoveInButtonActionPerformed

    private void deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteActionPerformed
     DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
int selectedRow = studentTable.getSelectedRow();

if (selectedRow == -1) {
    JOptionPane.showMessageDialog(this, "Select a row to delete!");
    return;
}

// Confirm deletion
int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this student?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
if (confirm != JOptionPane.YES_OPTION) {
    return;
}

// Get the student ID (Column 2 is assumed to be student_id)
String studentID = model.getValueAt(selectedRow, 2).toString();

try {
    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/dormitory_db", "root", "");
    String sql = "DELETE FROM student_info WHERE student_id = ?";
    PreparedStatement pstmt = con.prepareStatement(sql);
    pstmt.setString(1, studentID);
    int rowsAffected = pstmt.executeUpdate();
    pstmt.close();
    con.close();

    // Always remove the row from the table, even if DB deletion failed
    model.removeRow(selectedRow);

    if (rowsAffected > 0) {
        JOptionPane.showMessageDialog(this, "Student deleted successfully.");
    } else {
        JOptionPane.showMessageDialog(this, "Student not found in the database, but removed from the table.");
    }

} catch (Exception ex) {
    ex.printStackTrace();
    JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
    return;
}

saveToFile();
clearFields();
syncToPaymentTable();
updateRoomComboBox();

    }//GEN-LAST:event_deleteActionPerformed

    private void updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateActionPerformed
        DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a row to update!");
            return;
        }
        if (!validateInputs()) {
            return;
        }

        model.setValueAt(s_name.getText(), selectedRow, 0);
        model.setValueAt(s_bday.getText(), selectedRow, 1);
        model.setValueAt(s_id.getText(), selectedRow, 2);
        model.setValueAt(course.getSelectedItem().toString(), selectedRow, 3);
        model.setValueAt(gender.getSelectedItem().toString(), selectedRow, 4);
        model.setValueAt(phoneno.getText(), selectedRow, 5);
        model.setValueAt(address.getText(), selectedRow, 6);
        model.setValueAt(room1.getSelectedItem().toString(), selectedRow, 7);
        

        saveToFile();
        clearFields();
        syncToPaymentTable();
        updateRoomComboBox();
    }//GEN-LAST:event_updateActionPerformed

    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        clearFields();
    }//GEN-LAST:event_clearActionPerformed

    private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
        if (!validateInputs()) {
        return;
    }

    DefaultTableModel model = (DefaultTableModel) studentTable.getModel();
    String studentID = s_id.getText().trim();
    String studentName = s_name.getText().trim();

    // 🔁 Check for duplicates in the table
    for (int i = 0; i < model.getRowCount(); i++) {
        String existingID = model.getValueAt(i, 2).toString(); // Column 2 = student ID
        String existingName = model.getValueAt(i, 0).toString(); // Column 0 = student name

        if (existingID.equalsIgnoreCase(studentID) || existingName.equalsIgnoreCase(studentName)) {
            JOptionPane.showMessageDialog(this, "Student data already exists.");
            return;
        }
    }

    String paidStatus = "Pending";

    model.addRow(new Object[]{
        studentName,
        s_bday.getText(),
        studentID,
        course.getSelectedItem().toString(),
        gender.getSelectedItem().toString(),
        phoneno.getText(),
        address.getText(),
        room1.getSelectedItem().toString(),
        paidStatus
    });

    try {
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/dormitory_db", "root", "");
        String sql = "INSERT INTO student_info (student_name, date_of_birth, student_id, student_course, student_gender, phone_number, student_address, room_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setString(1, studentName);
        pstmt.setString(2, s_bday.getText());
        pstmt.setString(3, studentID);
        pstmt.setString(4, course.getSelectedItem().toString());
        pstmt.setString(5, gender.getSelectedItem().toString());
        pstmt.setString(6, phoneno.getText());
        pstmt.setString(7, address.getText());
        pstmt.setString(8, room1.getSelectedItem().toString());

        pstmt.executeUpdate();
        pstmt.close();
        con.close();

        // ✅ Show success message after successful DB insert
        JOptionPane.showMessageDialog(this, "Student data added successfully!");

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
    }

    saveToFile();
    clearFields();
    syncToPaymentTable();
    updateRoomComboBox();
    }//GEN-LAST:event_addActionPerformed

    private void phonenoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_phonenoKeyTyped
        if(!Character.isDigit(evt.getKeyChar())){
            evt.consume();
        }
    }//GEN-LAST:event_phonenoKeyTyped

    private void phonenoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phonenoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_phonenoActionPerformed

    private void s_nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_s_nameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_s_nameActionPerformed

    private void tabbedPaneKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tabbedPaneKeyPressed
        
    }//GEN-LAST:event_tabbedPaneKeyPressed

    private void tabbedPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabbedPaneMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tabbedPaneMouseClicked

    private void BookedTableFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_BookedTableFocusGained
        // TODO add your handling code here:
    }//GEN-LAST:event_BookedTableFocusGained

    private void tabbedPaneComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_tabbedPaneComponentShown
        // TODO add your handling code here:
    }//GEN-LAST:event_tabbedPaneComponentShown

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_tabbedPaneStateChanged

    private void jPanel11FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jPanel11FocusGained
        syncToPaymentTable();
        updateRoomComboBox();
    }//GEN-LAST:event_jPanel11FocusGained
private void populateFieldsFromStudentTable() {
    int selectedRow = studentTable.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select a student from the table.");
        return;
    }

    String name = studentTable.getValueAt(selectedRow, 0).toString();
    String studentId = studentTable.getValueAt(selectedRow, 2).toString();
    String addressText = studentTable.getValueAt(selectedRow, 6).toString();
    String room = studentTable.getValueAt(selectedRow, 7).toString();

    String[] names = name.split(" ", 2);
    String firstName = names.length > 0 ? names[0] : "";
    String lastName = names.length > 1 ? names[1] : "";

    studentIDField.setText(studentId);
    firstnameField.setText(firstName);
    surnameField.setText(lastName);
    addressField.setText(addressText);
    roomComboBox.setText(room);

}

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    populateFieldsFromStudentTable();
    int receiptTabIndex = tabbedPane.indexOfTab("Receipt");
    tabbedPane.setSelectedIndex(receiptTabIndex);

    }//GEN-LAST:event_jButton1ActionPerformed

    private void roomComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_roomComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_roomComboBoxActionPerformed

    private void roomComboBoxKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_roomComboBoxKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_roomComboBoxKeyTyped

    private void courseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_courseActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_courseActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
       syncToPaymentTable();
        updateRoomComboBox();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        DefaultTableModel ob=(DefaultTableModel) studentTable.getModel();
        TableRowSorter<DefaultTableModel> obj=new TableRowSorter<>(ob);
        studentTable.setRowSorter(obj);
        obj.setRowFilter(RowFilter.regexFilter(jTextField1.getText()));
    }//GEN-LAST:event_jTextField1KeyReleased

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows UI".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        java.awt.EventQueue.invokeLater(() -> {
            new Dashboard().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable AvailableTable;
    private javax.swing.JTable BookedTable;
    private javax.swing.JTable UnavailableTable;
    private javax.swing.JButton add;
    private javax.swing.JTextField address;
    private javax.swing.JTextField addressField;
    private javax.swing.JButton btnMoveBackToBooked;
    private javax.swing.JButton clear;
    private javax.swing.JButton confirmMoveInButton;
    private javax.swing.JTextField costField;
    private javax.swing.JComboBox<String> course;
    private javax.swing.JButton delete;
    private javax.swing.JTextField depositField;
    private javax.swing.JTextField downPaymentField;
    private javax.swing.JTextField electricityField;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JTextField firstnameField;
    private javax.swing.JComboBox<String> gender;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField phoneno;
    private javax.swing.JTextArea receiptArea;
    private javax.swing.JButton rentalButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JComboBox<String> room1;
    private javax.swing.JTextField roomComboBox;
    private javax.swing.JTextField s_bday;
    private javax.swing.JTextField s_id;
    private javax.swing.JTextField s_name;
    private javax.swing.JTextField studentIDField;
    private javax.swing.JTable studentTable;
    private javax.swing.JTextField surnameField;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTextField taxField;
    private javax.swing.JTextField totalPaymentField;
    private javax.swing.JButton update;
    private javax.swing.JTextField waterField;
    // End of variables declaration//GEN-END:variables
}
