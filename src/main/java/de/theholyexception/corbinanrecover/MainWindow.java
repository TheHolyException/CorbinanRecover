package de.theholyexception.corbinanrecover;

import de.theholyexception.holyapi.util.backuprecover.BackupItem;
import de.theholyexception.holyapi.util.backuprecover.BackupItemInfo;
import de.theholyexception.holyapi.util.backuprecover.BackupManager;
import de.theholyexception.holyapi.util.drophandler.DropHandler;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;

public class MainWindow extends JFrame {

	private JPanel contentPane;
	private JTextField textFieldSource;
	private JTextField textFieldTarget;
	private JTextField textFieldOffset;

	private JLabel lblSource;
	private JLabel lblOutput;
	private JLabel lblOffset;
	private JLabel lblBackupSelection;
	private JLabel lblPassword;
	private JLabel lblResolver;

	private JButton btnCheck;
	private JButton btnApply;
	private JComboBox<BackupItemInfoCC> comboBox;

	private BackupManager backupManager;
	private JPasswordField passwordField;
	private JComboBox comboBoxResolver;

	public MainWindow(String defaultSource, String defaultTarget, String password) {

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 250);
		setTitle("Backup recovery");
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		btnApply = new JButton("Apply");
		btnCheck = new JButton("Check");

		btnApply.setEnabled(false);
		btnCheck.setEnabled(true);

		textFieldSource = new JTextField("\\\\192.168.178.112\\ServerBackup\\E\\Program Files");
		textFieldSource.setColumns(10);
		if (defaultSource != null) textFieldSource.setText(defaultSource);

		textFieldTarget = new JTextField("E:\\output.zip");
		textFieldTarget.setColumns(10);
		if (defaultTarget != null) textFieldTarget.setText(defaultTarget);

		textFieldOffset = new JTextField("");
		textFieldOffset.setColumns(10);

		passwordField = new JPasswordField();
		if (password != null) passwordField.setText(password);

		lblSource = new JLabel("Source");
		lblOutput = new JLabel("Output");
		lblOffset = new JLabel("Start point");
		lblPassword = new JLabel("Zip Password");
		lblResolver = new JLabel("Resolver");
		lblBackupSelection = new JLabel("Backup");

		comboBox = new JComboBox<>();
		comboBoxResolver = new JComboBox<BackupResolver>();

		comboBoxResolver.addItem(new BackupResolver(BackupManager.Resolvers.CORBINAN_EN, "CORBINAN EN"));
		comboBoxResolver.addItem(new BackupResolver(BackupManager.Resolvers.CORBINAN_DE, "CORBINAN DE"));

		btnCheck.addActionListener(l -> {
			comboBox.removeAllItems();
			File source = new File(textFieldSource.getText());

			Map<String, List<BackupItemInfo>> items;
			try {
				items = BackupManager.scanSourceDirectory(source, ((BackupResolver) comboBoxResolver.getSelectedItem()).getResolver());
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}

			items.forEach((k, v) -> v.forEach(e -> {
				comboBox.addItem(new BackupItemInfoCC(e));
			}));
			btnApply.setEnabled(true);
		});

		btnApply.addActionListener(l -> {
			File source = new File(textFieldSource.getText());
			File target = new File(textFieldTarget.getText());

			BackupItemInfoCC itemInfoCC = (BackupItemInfoCC) comboBox.getSelectedItem();
			new BackupManager(source, target, ((BackupResolver) comboBoxResolver.getSelectedItem()).getResolver()).restore(itemInfoCC.getPath(), itemInfoCC.getTimeStamp(), passwordField.getPassword(), textFieldOffset.getText());
		});

		setupLayout();
/*		contentPane.setLayout(new GridLayout(5, 3));
		contentPane.add(lblSource);
		contentPane.add(textFieldSource);

		contentPane.add(lblBackupSelection);
		contentPane.add(comboBox);

		contentPane.add(lblResolver);
		contentPane.add()*/
		setVisible(true);

		new DropHandler(textFieldSource, action -> textFieldSource.setText(action.getFiles().stream().toList().get(0).getAbsolutePath()));
		new DropHandler(textFieldTarget, action -> textFieldTarget.setText(action.getFiles().stream().toList().get(0).getAbsolutePath()));

	}

	private void setupLayout() {



		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(lblSource, GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
								.addComponent(lblOutput, GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
								.addComponent(lblOffset, GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
								.addComponent(lblBackupSelection)
								.addComponent(lblResolver))
							.addPreferredGap(ComponentPlacement.RELATED))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblPassword)
							.addGap(33)))
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(textFieldSource, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
						.addComponent(textFieldTarget, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
						.addComponent(textFieldOffset, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
						.addComponent(comboBox, Alignment.TRAILING, 0, 308, Short.MAX_VALUE)
						.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
								.addComponent(passwordField, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
								.addComponent(comboBoxResolver, 0, 179, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(btnApply, GroupLayout.PREFERRED_SIZE, 119, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnCheck, GroupLayout.PREFERRED_SIZE, 119, GroupLayout.PREFERRED_SIZE))))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblSource)
						.addComponent(textFieldSource, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblBackupSelection))
					.addGap(8)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(textFieldTarget, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblOutput))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(textFieldOffset, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblOffset))
					.addGap(8)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(btnCheck)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
							.addComponent(comboBoxResolver, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblResolver)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPassword)
						.addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnApply))
					.addContainerGap(29, Short.MAX_VALUE))
		);
		contentPane.setLayout(gl_contentPane);
	}
}
