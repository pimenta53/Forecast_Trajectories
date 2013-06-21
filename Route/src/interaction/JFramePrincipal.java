package interaction;

import terrain.*;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import ontology.Point2D;

import java.awt.Font;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;
import java.awt.GridLayout;

public class JFramePrincipal extends JFrame implements Observer {

	private JPanel contentPane;
	private JPanel[][] matrix;
	public static Terrain terreno;
	public static ArrayList<Point2D> sensores;
	private static int matrixHeight;
	private static int matrixWidth;
	private JTextField textFieldStartSensores;
	private JTextField textFieldMaxSensores;
	private JTextField textFieldSpeed;
	private JTextField textFieldMoves;
	private static Runtime rt;
	public static ContainerController cc;
	private int numIt = 0;
	public static boolean end = true, set = false;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JFramePrincipal frame = new JFramePrincipal();
					frame.setTitle("Agentes Inteligentes");
					frame.setVisible(true);
					frame.setResizable(false);
					
					//Inicializacao da plataforma JADE (pode se utilizar o metodo jade boot)
					rt = Runtime.instance();
					Profile p = new ProfileImpl();
					p.setParameter(Profile.MAIN_HOST, "localhost");
					p.setParameter(Profile.MAIN_PORT, "80");
					cc = rt.createMainContainer(p);
				} catch (Exception e) {e.printStackTrace();}
			}
		});
	}
	
	/**
	 * Create the frame.
	 */
	public JFramePrincipal() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		terreno = new Terrain();
		sensores = new ArrayList<Point2D>();
		terreno.addObserver(this);
		matrixWidth = terreno.lengthW();
		matrixHeight = terreno.lengthH();
		
		/*Termina a plataforma*/
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				rt.shutDown();
			}
		});
		
		Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
	    setBounds(0,0,winSize.width, winSize.height);
		
		/*Components*/
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{15, 0, 0, 0, 0, 0, 0, 0, 0, 50, 0};
		gbl_contentPane.rowHeights = new int[]{50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		File folder = new File("maps");
		FilenameFilter ff = new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				if (name.contains(".txt")) return true;
				else return false;
			}
		};
		String[] pattern = folder.list(ff);
	
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final JComboBox comboBoxSelectTerreno = new JComboBox(pattern);
		comboBoxSelectTerreno.setSelectedIndex(-1);
		comboBoxSelectTerreno.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		
		final JPanel matrixTerreno = new JPanel();
		GridBagConstraints gbc_matrixTerreno = new GridBagConstraints();
		gbc_matrixTerreno.gridheight = 11;
		gbc_matrixTerreno.gridwidth = 3;
		gbc_matrixTerreno.anchor = GridBagConstraints.NORTHWEST;
		gbc_matrixTerreno.insets = new Insets(0, 0, 5, 5);
		gbc_matrixTerreno.gridx = 1;
		gbc_matrixTerreno.gridy = 1;
		contentPane.add(matrixTerreno, gbc_matrixTerreno);
		matrixTerreno.setLayout(new GridLayout(80,80));
		
		JButton btnNewButton = new JButton("Set Sensors");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (comboBoxSelectTerreno.getSelectedIndex() == -1)
					JOptionPane.showMessageDialog(null, "Pick a terrain");
				else if (Integer.parseInt(textFieldStartSensores.getText()) < 2) {
						JOptionPane.showMessageDialog(null, "The number of starting sensors needs to be 2 or higher");
						comboBoxSelectTerreno.setSelectedIndex(-1);
					 }
				else if (!end)
						JOptionPane.showMessageDialog(null, "Wait for the walker agent to finish his path");
				else if (set)
					JOptionPane.showMessageDialog(null, "The sensors have already been set. Start the walker agent");
				else{ 
					if(numIt == 0){
						try{
							//Obter os valores dos sensores
							int numSensores = Integer.parseInt(textFieldStartSensores.getText());
							int maxSensores = Integer.parseInt(textFieldMaxSensores.getText());
							if (numSensores > maxSensores){
								JOptionPane.showMessageDialog(null, "The number of starting sensors cannot be higher than the number of max sensors");
								comboBoxSelectTerreno.setSelectedIndex(-1);
							}
							// Criar o objecto para passar como argumento ao agente sensorCommunication
							Object argS[] = new Object[2];
							argS[0] = numSensores;
							argS[1] = maxSensores;
							//Inicializa o supervisor
							AgentController supervisor = cc.createNewAgent("SuperVisor", "terrain.Supervisor", argS);
							supervisor.start();
							//Inicia a central dos sensores
							AgentController centralSensor = cc.createNewAgent("SensorCommunication", "terrain.Sensor_Communication", argS);
							centralSensor.start();
							textFieldStartSensores.setEnabled(false);
							textFieldMaxSensores.setEnabled(false);
							Thread.sleep(2000);
							for (Point2D pt : sensores)
								terreno.setSensor(pt.getX(), pt.getY(), Terrain.range, 0);
						}catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null,"Enter the number of starting and max sensors");
							comboBoxSelectTerreno.setSelectedIndex(-1);
						}catch (Exception e) {if (!(e instanceof NullPointerException)) e.printStackTrace();}
					}
					else{
						String item = (String) comboBoxSelectTerreno.getSelectedItem();
						String ficheiro = new String("maps/".concat(item));
						terreno.loadTerrain(ficheiro);
						paintTerrain(matrix);
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {e.printStackTrace();}
						for (Point2D pt : sensores)
							terreno.setSensor(pt.getX(), pt.getY(), Terrain.range, 0);
					}
					set = true;
				}
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.SOUTH;
		gbc_btnNewButton.gridwidth = 6;
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.ipadx = 10;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 4;
		gbc_btnNewButton.gridy = 8;
		contentPane.add(btnNewButton, gbc_btnNewButton);
		
		final JLabel lblCountIt = new JLabel(""+numIt);
		lblCountIt.setFont(new Font("Times New Roman", Font.ITALIC, 16));
		GridBagConstraints gbc_lblCountIt = new GridBagConstraints();
		gbc_lblCountIt.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblCountIt.insets = new Insets(0, 0, 5, 5);
		gbc_lblCountIt.gridx = 8;
		gbc_lblCountIt.gridy = 10;
		contentPane.add(lblCountIt, gbc_lblCountIt);
		
		createMatrix(matrixTerreno);
		
		JButton btnStart = new JButton("Start");
		btnStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (comboBoxSelectTerreno.getSelectedIndex() == -1)
					JOptionPane.showMessageDialog(null,"Pick a terrain");
				else if (!set && numIt == 0)
					JOptionPane.showMessageDialog(null,"First set the sensors on the terrain");
				else if (numIt > 0)
					JOptionPane.showMessageDialog(null,"Set the sensors and press Next");
				else{
					//Criar agente walker
					try {
						int numMoves = Integer.parseInt(textFieldMoves.getText());
						int numSpeed = Integer.parseInt(textFieldSpeed.getText());
						boolean numOK = true;
						long sp = 1;
						switch(numSpeed){
						case 1:
							sp = 500;
							numOK = true;
							break;
						case 2:
							sp = 250;
							numOK = true;
							break;
						case 3:
							sp = 125;
							numOK = true;
							break;
						case 4:
							sp = 75;
							numOK = true;
							break;
						case 5:
							sp = 40;
							numOK = true;
							break;
						default :
							numOK = false;
							JOptionPane.showMessageDialog(null, "The speed value must be between 1 and 5");
							break;
						}
						if(numOK){
							Object argW[] = new Object[2];
							argW[0] = numMoves;
							argW[1] = sp;
							AgentController walker = cc.createNewAgent("walker", "routes.WalkerAgent", argW);
							walker.start();
							numIt = 1;
							lblCountIt.setText(""+numIt);
							set = false;
						}
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null,"Enter the desired number of moves and speed");}
					  catch (StaleProxyException e) {
						JOptionPane.showMessageDialog(null,"The walker agent already performed his path");}
					  catch (Exception e) {e.printStackTrace();}
				}
			}
		});
		
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.gridwidth = 2;
		gbc_btnStart.ipadx = 50;
		gbc_btnStart.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnStart.ipady = 40;
		gbc_btnStart.insets = new Insets(0, 0, 5, 5);
		gbc_btnStart.gridx = 4;
		gbc_btnStart.gridy = 9;
		contentPane.add(btnStart, gbc_btnStart);
		
		// Inicia uma nova iteraçao do agente 
		JButton btnNext = new JButton("Next");
		btnNext.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (comboBoxSelectTerreno.getSelectedIndex() == -1)
					JOptionPane.showMessageDialog(null, "Pick a terrain");
				else if (!end)
						JOptionPane.showMessageDialog(null, "Wait for the walker agent to finish his path");
				else if (!set)
					JOptionPane.showMessageDialog(null,"First set the sensors on the terrain");
				else{
					try {
						AgentController walker = cc.getAgent("walker");
							walker.kill();
							int numMoves = Integer.parseInt(textFieldMoves.getText());
							int numSpeed = Integer.parseInt(textFieldSpeed.getText());
							boolean numOK = true;
							long sp = 1;
							switch(numSpeed){
							case 1:
								sp = 500;
								numOK = true;
								break;
							case 2:
								sp = 250;
								numOK = true;
								break;
							case 3:
								sp = 125;
								numOK = true;
								break;
							case 4:
								sp = 75;
								numOK = true;
								break;
							case 5:
								sp = 40;
								numOK = true;
								break;
							default :
								numOK = false;
								JOptionPane.showMessageDialog(null, "The speed value must be between 1 and 5");
								break;
							}
							if(numOK){
								Object argW[] = new Object[2];
								argW[0] = numMoves;
								argW[1] = sp;
								try {
									Thread.sleep(50);
								} catch (InterruptedException e1) {e1.printStackTrace();}
								walker = cc.createNewAgent("walker", "routes.WalkerAgent", argW);
								walker.start();
								numIt++;
								lblCountIt.setText(""+numIt);
								set = false;
							}
					} catch (ControllerException e1) {JOptionPane.showMessageDialog(null, "First press Start");}
				}
			}
		});
		
		GridBagConstraints gbc_btnNext = new GridBagConstraints();
		gbc_btnNext.gridwidth = 2;
		gbc_btnNext.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNext.ipadx = 50;
		gbc_btnNext.ipady = 40;
		gbc_btnNext.insets = new Insets(0, 0, 5, 5);
		gbc_btnNext.gridx = 6;
		gbc_btnNext.gridy = 9;
		contentPane.add(btnNext, gbc_btnNext);
		
		JLabel lblNumIt = new JLabel("Number of iterations:");
		lblNumIt.setFont(new Font("Times New Roman", Font.ITALIC, 16));
		GridBagConstraints gbc_lblNumIt = new GridBagConstraints();
		gbc_lblNumIt.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblNumIt.insets = new Insets(0, 0, 5, 5);
		gbc_lblNumIt.gridx = 7;
		gbc_lblNumIt.gridy = 10;
		contentPane.add(lblNumIt, gbc_lblNumIt);
		
		JLabel lblEscolhaUmTerreno = new JLabel("Pick a Terrain:");
		lblEscolhaUmTerreno.setFont(new Font("Times New Roman", Font.ITALIC, 16));
		GridBagConstraints gbc_lblEscolhaUmTerreno = new GridBagConstraints();
		gbc_lblEscolhaUmTerreno.gridwidth = 2;
		gbc_lblEscolhaUmTerreno.insets = new Insets(0, 0, 5, 5);
		gbc_lblEscolhaUmTerreno.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblEscolhaUmTerreno.gridx = 5;
		gbc_lblEscolhaUmTerreno.gridy = 1;
		contentPane.add(lblEscolhaUmTerreno, gbc_lblEscolhaUmTerreno);
		
		comboBoxSelectTerreno.addActionListener(new ActionListener() {
			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			public void actionPerformed(ActionEvent arg0) {
				if(comboBoxSelectTerreno.getSelectedIndex() != -1){
					String item = (String) comboBoxSelectTerreno.getSelectedItem();
				String ficheiro = new String("maps/".concat(item));
				terreno.loadTerrain(ficheiro);
				paintTerrain(matrix);
				comboBoxSelectTerreno.setEnabled(false);
				}
			}
		});
		GridBagConstraints gbc_comboBoxSelectTerreno = new GridBagConstraints();
		gbc_comboBoxSelectTerreno.gridwidth = 2;
		gbc_comboBoxSelectTerreno.anchor = GridBagConstraints.NORTH;
		gbc_comboBoxSelectTerreno.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxSelectTerreno.insets = new Insets(0, 0, 5, 5);
		gbc_comboBoxSelectTerreno.gridx = 7;
		gbc_comboBoxSelectTerreno.gridy = 1;
		contentPane.add(comboBoxSelectTerreno, gbc_comboBoxSelectTerreno);
		
		JButton btnEnd = new JButton("End");
		btnEnd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (comboBoxSelectTerreno.getSelectedIndex() == -1)
					JOptionPane.showMessageDialog(null, "Nothing has been initialized");
				else {
					//voltar a desenhar a matriz sem nada
					comboBoxSelectTerreno.setSelectedIndex(-1);
					comboBoxSelectTerreno.setEnabled(true);
					textFieldStartSensores.setEnabled(true);
					textFieldMaxSensores.setEnabled(true);
					end = true;
					set = false;
					textFieldMaxSensores.setText("5");
					textFieldMoves.setText("30");
					textFieldSpeed.setText("4");
					textFieldStartSensores.setText("2");
					for (int i = 0; i < matrixHeight; i++)
						for (int j = 0; j < matrixWidth; j++)
							terreno.setCell(i, j, null);
					paintTerrain(matrix);
					sensores.clear();
					numIt = 0;
					lblCountIt.setText(""+numIt);
					
					//Mata os agentes existentes no container
					try{
						AgentController central = cc.getAgent("SensorCommunication");
						central.kill();
						AgentController supervisor = cc.getAgent("SuperVisor");
						supervisor.kill();
						AgentController walker = cc.getAgent("walker");
						walker.kill();
					}
					catch (Exception e1) {}
				}
			}
		});
		
		JLabel lblSensores = new JLabel("Sensors");
		lblSensores.setFont(new Font("Times New Roman", Font.BOLD, 24));
		GridBagConstraints gbc_lblSensores = new GridBagConstraints();
		gbc_lblSensores.gridwidth = 2;
		gbc_lblSensores.anchor = GridBagConstraints.SOUTH;
		gbc_lblSensores.insets = new Insets(0, 0, 5, 5);
		gbc_lblSensores.gridx = 5;
		gbc_lblSensores.gridy = 5;
		contentPane.add(lblSensores, gbc_lblSensores);
		
		JLabel lblWalker = new JLabel("Walker");
		lblWalker.setFont(new Font("Times New Roman", Font.BOLD, 24));
		GridBagConstraints gbc_lblWalker = new GridBagConstraints();
		gbc_lblWalker.anchor = GridBagConstraints.SOUTH;
		gbc_lblWalker.gridwidth = 2;
		gbc_lblWalker.insets = new Insets(0, 0, 5, 5);
		gbc_lblWalker.gridx = 7;
		gbc_lblWalker.gridy = 5;
		contentPane.add(lblWalker, gbc_lblWalker);
		
		JLabel lblStartSensors = new JLabel("Start Sensors");
		lblStartSensors.setFont(new Font("Traditional Arabic", Font.PLAIN, 16));
		GridBagConstraints gbc_lblStartSensors = new GridBagConstraints();
		gbc_lblStartSensors.anchor = GridBagConstraints.EAST;
		gbc_lblStartSensors.insets = new Insets(0, 0, 5, 5);
		gbc_lblStartSensors.gridx = 5;
		gbc_lblStartSensors.gridy = 6;
		contentPane.add(lblStartSensors, gbc_lblStartSensors);
		
		textFieldStartSensores = new JTextField("2");
		GridBagConstraints gbc_textFieldStartSensores = new GridBagConstraints();
		gbc_textFieldStartSensores.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldStartSensores.ipadx = 50;
		gbc_textFieldStartSensores.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldStartSensores.gridx = 6;
		gbc_textFieldStartSensores.gridy = 6;
		contentPane.add(textFieldStartSensores, gbc_textFieldStartSensores);
		textFieldStartSensores.setColumns(10);
		textFieldStartSensores.setHorizontalAlignment(JTextField.CENTER);
		
		JLabel lblSpeed = new JLabel("Speed");
		lblSpeed.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		GridBagConstraints gbc_lblSpeed = new GridBagConstraints();
		gbc_lblSpeed.anchor = GridBagConstraints.EAST;
		gbc_lblSpeed.insets = new Insets(0, 0, 5, 5);
		gbc_lblSpeed.gridx = 7;
		gbc_lblSpeed.gridy = 6;
		contentPane.add(lblSpeed, gbc_lblSpeed);
		
		textFieldSpeed = new JTextField("4");
		GridBagConstraints gbc_textFieldSpeed = new GridBagConstraints();
		gbc_textFieldSpeed.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldSpeed.ipadx = 50;
		gbc_textFieldSpeed.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldSpeed.gridx = 8;
		gbc_textFieldSpeed.gridy = 6;
		contentPane.add(textFieldSpeed, gbc_textFieldSpeed);
		textFieldSpeed.setColumns(10);
		textFieldSpeed.setHorizontalAlignment(JTextField.CENTER);
		
		JLabel lblMaxSensors = new JLabel("Max Sensors");
		lblMaxSensors.setFont(new Font("Traditional Arabic", Font.PLAIN, 16));
		GridBagConstraints gbc_lblMaxSensors = new GridBagConstraints();
		gbc_lblMaxSensors.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblMaxSensors.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxSensors.gridx = 5;
		gbc_lblMaxSensors.gridy = 7;
		contentPane.add(lblMaxSensors, gbc_lblMaxSensors);
		
		textFieldMaxSensores = new JTextField("5");
		GridBagConstraints gbc_textFieldMaxSensores = new GridBagConstraints();
		gbc_textFieldMaxSensores.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldMaxSensores.ipadx = 50;
		gbc_textFieldMaxSensores.anchor = GridBagConstraints.NORTH;
		gbc_textFieldMaxSensores.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldMaxSensores.gridx = 6;
		gbc_textFieldMaxSensores.gridy = 7;
		contentPane.add(textFieldMaxSensores, gbc_textFieldMaxSensores);
		textFieldMaxSensores.setColumns(10);
		textFieldMaxSensores.setHorizontalAlignment(JTextField.CENTER);
		
		JLabel lblMoves = new JLabel("Moves");
		lblMoves.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		GridBagConstraints gbc_lblMoves = new GridBagConstraints();
		gbc_lblMoves.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblMoves.insets = new Insets(0, 0, 5, 5);
		gbc_lblMoves.gridx = 7;
		gbc_lblMoves.gridy = 7;
		contentPane.add(lblMoves, gbc_lblMoves);
		
		textFieldMoves = new JTextField("30");
		GridBagConstraints gbc_textFieldMoves = new GridBagConstraints();
		gbc_textFieldMoves.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldMoves.ipadx = 50;
		gbc_textFieldMoves.anchor = GridBagConstraints.NORTH;
		gbc_textFieldMoves.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldMoves.gridx = 8;
		gbc_textFieldMoves.gridy = 7;
		contentPane.add(textFieldMoves, gbc_textFieldMoves);
		textFieldMoves.setColumns(10);
		textFieldMoves.setHorizontalAlignment(JTextField.CENTER);
		
		GridBagConstraints gbc_btnEnd = new GridBagConstraints();
		gbc_btnEnd.gridwidth = 2;
		gbc_btnEnd.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnEnd.ipadx = 50;
		gbc_btnEnd.ipady = 40;
		gbc_btnEnd.insets = new Insets(0, 0, 5, 5);
		gbc_btnEnd.gridx = 8;
		gbc_btnEnd.gridy = 9;
		contentPane.add(btnEnd, gbc_btnEnd);
	}
	
	public void createMatrix(JPanel matrixTerreno) {
		matrix = new JPanel[matrixHeight][matrixWidth];
		for(int i = 0; i < matrixHeight; i++){
			for(int j = 0; j < matrixWidth; j++){
				matrix[i][j] = new JPanel();
				matrix[i][j].setBorder(BorderFactory.createLineBorder(Color.black));
				matrixTerreno.add(matrix[i][j]);
			}
		}
	}
	
	public void paintTerrain(JPanel[][] jp){
		for(int i = 0; i < matrixHeight; i++){
			for(int j = 0; j < matrixWidth; j++){
				try{
					String ex = new String(terreno.getCell(i, j));
					switch (ex.charAt(0)) {
					case 't':
						jp[i][j].setBackground(Color.GREEN);
						break;
					case 'v':
						jp[i][j].setBackground(Color.getHSBColor(0, (float)1, (float)1));
						break;
					case 's':
						jp[i][j].setBackground(Color.WHITE);
						break;
					case 'p':
						jp[i][j].setBackground(Color.BLACK);
						break;
					case 'a':
						jp[i][j].setBackground(Color.YELLOW);
						break;
					case 'w':
						jp[i][j].setBackground(Color.BLUE);
						break;
					case 'c':
						jp[i][j].setBackground(Color.CYAN);
						break;
					case 'i':
						jp[i][j].setBackground(Color.ORANGE);
						break;
					case'f':
						jp[i][j].setBackground(Color.DARK_GRAY);
						break;
					default:
						jp[i][j].setBackground(Color.GRAY);
						break;
					}
				}
				catch (NullPointerException e) {jp[i][j].setBackground(Color.getHSBColor(0, 0, (float)0.93));}
			}
		}
	}
	
	public void paintCell(JPanel[][] jp, int i, int j){
		try{
			String ex = new String(terreno.getCell(i, j));
			switch (ex.charAt(0)) {
			case 't':
				jp[i][j].setBackground(Color.GREEN);
				break;
			case 'v':
				jp[i][j].setBackground(Color.getHSBColor(0, (float)1, (float)1));
				break;
			case 's':
				jp[i][j].setBackground(Color.WHITE);
				break;
			case 'p':
				jp[i][j].setBackground(Color.BLACK);
				break;
			case 'a':
				jp[i][j].setBackground(Color.YELLOW);
				break;
			case 'w':
				jp[i][j].setBackground(Color.BLUE);
				break;
			case 'c':
				jp[i][j].setBackground(Color.CYAN);
				break;
			case 'i':
				jp[i][j].setBackground(Color.ORANGE);
				break;
			case'f':
				jp[i][j].setBackground(Color.DARK_GRAY);
				break;
			default:
				jp[i][j].setBackground(Color.GRAY);
				break;
			}
		}
		catch (NullPointerException e) {jp[i][j].setBackground(Color.getHSBColor(0, 0, (float)0.93));}
	}
	
	public void update(Observable obs, Object obj){
		
			String arg = (String)obj;
			Scanner lineScan = new Scanner(arg);
			lineScan.useDelimiter(",");
			
			int coord[] = new int[2];
			int i = 0;
			
			while (lineScan.hasNext()){
				coord[i] = lineScan.nextInt();
				i++;
			}

			paintCell(matrix, coord[0], coord[1]);
	}
	
}