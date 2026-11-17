package com.vaxicare.ui;

import com.vaxicare.model.Beneficiary;
import com.vaxicare.model.DoseRecord;
import com.vaxicare.service.VaxiCareService;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * Pure AWT Modal Dialog displaying official vaccination certificate with export and clipboard capabilities.
 * Demonstrates Unit IV: java.awt.Dialog, Clipboard integration, and AWT event delegation.
 */
public class CertificateDialog extends Dialog implements ActionListener {

    private final DoseRecord record;
    private final Beneficiary beneficiary;
    private final VaxiCareService service;

    private TextArea txtCert;
    private Button btnSaveFile;
    private Button btnCopyClipboard;
    private Button btnClose;
    private Label lblStatus;

    public CertificateDialog(Frame parent, DoseRecord record, Beneficiary beneficiary, VaxiCareService service) {
        super(parent, "VaxiCare Official Clinical Certificate - " + record.getCertificateNo(), true);
        this.record = record;
        this.beneficiary = beneficiary;
        this.service = service;

        initializeDialog();
    }

    private void initializeDialog() {
        setSize(620, 520);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(244, 241, 234)); // Soft cream

        // 1. Header
        Panel headerPanel = new Panel(new BorderLayout());
        headerPanel.setBackground(new Color(36, 43, 61)); // Deep Navy
        Label title = new Label("  VAXICARE DIGITAL VACCINATION CERTIFICATE", Label.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.CENTER);

        Label sub = new Label("Clinical Verification Engine • Standard Form VC-26   ", Label.RIGHT);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(new Color(190, 205, 225));
        headerPanel.add(sub, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Certificate Body (Monospaced TextArea)
        String certContent = service.formatCertificate(record, beneficiary);
        txtCert = new TextArea(certContent, 18, 70, TextArea.SCROLLBARS_VERTICAL_ONLY);
        txtCert.setEditable(false);
        txtCert.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtCert.setBackground(Color.WHITE);
        txtCert.setForeground(new Color(20, 30, 45));

        Panel centerPanel = new Panel(new BorderLayout(4, 4));
        centerPanel.add(txtCert, BorderLayout.CENTER);

        lblStatus = new Label(" Certificate authenticated and registered in SQLite database.");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(30, 111, 120));
        centerPanel.add(lblStatus, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Action Buttons Panel
        Panel btnPanel = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnPanel.setBackground(new Color(234, 230, 220));

        btnSaveFile = new Button("Save Certificate (.txt)");
        btnSaveFile.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSaveFile.setBackground(new Color(30, 111, 120)); // Teal
        btnSaveFile.setForeground(Color.WHITE);
        btnSaveFile.addActionListener(this);

        btnCopyClipboard = new Button("Copy to Clipboard");
        btnCopyClipboard.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnCopyClipboard.addActionListener(this);

        btnClose = new Button("Close");
        btnClose.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnClose.addActionListener(this);

        btnPanel.add(btnCopyClipboard);
        btnPanel.add(btnSaveFile);
        btnPanel.add(btnClose);
        add(btnPanel, BorderLayout.SOUTH);

        // Window closing adapter
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnClose) {
            dispose();
        } else if (src == btnCopyClipboard) {
            try {
                StringSelection sel = new StringSelection(txtCert.getText());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
                lblStatus.setText(" ✓ Certificate copied to system clipboard!");
                lblStatus.setForeground(new Color(22, 163, 74));
            } catch (Exception ex) {
                lblStatus.setText(" Failed to copy: " + ex.getMessage());
            }
        } else if (src == btnSaveFile) {
            try {
                String filename = String.format("certificate_%s_Dose%d.txt", record.getBenId(), record.getDoseNo());
                service.saveCertificateToFile(record, beneficiary, filename);
                File savedFile = new File(filename);
                lblStatus.setText(" ✓ Saved successfully to: " + savedFile.getAbsolutePath());
                lblStatus.setForeground(new Color(22, 163, 74));
            } catch (Exception ex) {
                lblStatus.setText(" Error saving certificate: " + ex.getMessage());
                lblStatus.setForeground(new Color(220, 38, 38));
            }
        }
    }
}
