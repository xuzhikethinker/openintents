/* 
 * Copyright (C) 2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.tools.sensorsimulator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/* SensorSimulator.java requires no other files. */
public class SensorSimulator extends JPanel
							implements ActionListener,
								WindowListener,
								ChangeListener,
								ItemListener {
	
	//static long serialVersionUID = 1234;

	// Supported sensors:
	static final String ACCELEROMETER = "accelerometer";
	static final String COMPASS = "compass";
	static final String ORIENTATION = "orientation";
	static final String THERMOMETER = "thermometer";
	
	static final String DISABLED = "DISABLED";
	
	
	int delay;
    Timer timer;
    
    int mouseMode;
    static int mouseYawPitch = 1;
    static int mouseRollPitch = 2;
    static int mouseMove = 3;
    
    // Displays the mobile phone
    MobilePanel mobile;
    
    // Sliders:
    JSlider yawSlider;
    JSlider pitchSlider;
    JSlider rollSlider;
    
    // Text fields:
    JTextField socketText;
    JButton socketButton;
    
    // Field for socket related output:
    JScrollPane areaScrollPane;
    JTextArea ipselectionText;
    
    // Field for sensor simulator data output:
    JScrollPane scrollPaneSensorData;
    JTextArea textAreaSensorData;
    
    // Settings
    // Supported sensors
    JCheckBox mSupportedAccelerometer;
    JCheckBox mSupportedCompass;
    JCheckBox mSupportedOrientation;
    JCheckBox mSupportedThermometer;
    
    // Enabled sensors
    JCheckBox mEnabledAccelerometer;
    JCheckBox mEnabledCompass;
    JCheckBox mEnabledOrientation;
    JCheckBox mEnabledThermometer;
    
    // Gravity
    JTextField mGravityXText;
    JTextField mGravityYText;
    JTextField mGravityZText;
    
    // Magnetic field
    JTextField mMagneticFieldNorthText;
    JTextField mMagneticFieldEastText;
    JTextField mMagneticFieldVerticalText;
    
    // Temperature
    JTextField mTemperatureText;
    
    // Action Commands:
    static String yawPitch = "yaw & pitch";
    static String rollPitch = "roll & pitch";
    static String move = "move";
    static String timerAction = "timer";
    static String setPortString = "set port";
    
    // Server for sending out sensor data
    SensorServer mSensorServer;
    int mIncomingConnections;
    
	
	public SensorSimulator() {
		// Initialize variables
		mIncomingConnections = 0;
		
		//setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setLayout(new BorderLayout());

		///////////////////////////////////////////////////////////////
        // Left pane
        //JPanel leftPane = new JPanel(new BorderLayout());
		
        GridBagLayout myGridBagLayout = new GridBagLayout();
        // myGridLayout.
        GridBagConstraints c = new GridBagConstraints();
        JPanel leftPane = new JPanel(myGridBagLayout);
        
        JPanel mobilePane = new JPanel(new BorderLayout());
        
		// Add the mobile
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        mobile = new MobilePanel(this);
        mobilePane.add(mobile);
        
        leftPane.add(mobilePane, c);
        
        // Add mouse action selection
        // through radio buttons.
        JRadioButton yawPitchButton = new JRadioButton(yawPitch);
        //yawTiltButton.setMnemonic(KeyEvent.VK_Y);
        yawPitchButton.setActionCommand(yawPitch);
        yawPitchButton.setSelected(true);
        mouseMode = mouseYawPitch;
        
        JRadioButton rollPitchButton = new JRadioButton(rollPitch);
        rollPitchButton.setActionCommand(rollPitch);
        
        JRadioButton moveButton = new JRadioButton(move);
        moveButton.setActionCommand(move);
        
        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(yawPitchButton);
        group.add(rollPitchButton);
        group.add(moveButton);
        
        //Register a listener for the radio buttons.
        yawPitchButton.addActionListener(this);
        rollPitchButton.addActionListener(this);
        moveButton.addActionListener(this);
        
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        leftPane.add(yawPitchButton, c);
        c.gridx++;
        leftPane.add(rollPitchButton, c);
        c.gridx++;
        leftPane.add(moveButton, c);
        
        // Add IP address properties:
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        JLabel socketLabel = new JLabel("Socket", JLabel.LEFT);
        leftPane.add(socketLabel, c);
        
        c.gridx = 1;
        socketText = new JTextField(5);
        leftPane.add(socketText, c);
        
        c.gridx = 2;
        socketButton = new JButton("Set");
        leftPane.add(socketButton, c);
        socketButton.setActionCommand(setPortString);
        socketButton.addActionListener(this);
        
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        ipselectionText = new JTextArea(3, 10);
        //Dimension d = new Dimension();
        //d.height = 50;
        //d.width = 200;
        //ipselectionText.setPreferredSize(d);
        //ipselectionText.setAutoscrolls(true);
        
        areaScrollPane = new JScrollPane(ipselectionText);
        areaScrollPane.setVerticalScrollBarPolicy(
        		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(250, 80));

        leftPane.add(areaScrollPane, c);
        
        
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        textAreaSensorData = new JTextArea(3, 10);
        scrollPaneSensorData = new JScrollPane(textAreaSensorData);
        scrollPaneSensorData.setVerticalScrollBarPolicy(
        		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPaneSensorData.setPreferredSize(new Dimension(250, 80));

        leftPane.add(scrollPaneSensorData, c);
        
        leftPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        ///////////////////////////////////////////////////////////////
        // Right pane
        JLabel simulatorLabel = new JLabel("OpenIntents Sensor Simulator", JLabel.CENTER);
        simulatorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        Font font = new Font("SansSerif", Font.PLAIN, 22);
        simulatorLabel.setFont(font);
        //Border border = new Border();
        //simulatorLabel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.blue));
        simulatorLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 20, 5));
        
        //Create the label.
		JLabel yawLabel = new JLabel("Yaw", JLabel.CENTER);
        yawLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel pitchLabel = new JLabel("Pitch", JLabel.CENTER);
        pitchLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel rollLabel = new JLabel("Roll", JLabel.CENTER);
        rollLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
	    //Create the slider.
        yawSlider = new JSlider(JSlider.HORIZONTAL, -180, 180, -20);
        pitchSlider = new JSlider(JSlider.HORIZONTAL, -180, 180, 60);
	    rollSlider = new JSlider(JSlider.HORIZONTAL, -180, 180, 0);
	    
	    yawSlider.addChangeListener(this);
	    pitchSlider.addChangeListener(this);
	    rollSlider.addChangeListener(this);
	    mobile.yawDegree = yawSlider.getValue();
	    mobile.pitchDegree = pitchSlider.getValue();
	    mobile.rollDegree = rollSlider.getValue();    
	    mobile.yawSlider = yawSlider;
	    mobile.pitchSlider = pitchSlider;
	    mobile.rollSlider = rollSlider;

		//Turn on labels at major tick marks.

	    yawSlider.setBorder(
                BorderFactory.createEmptyBorder(0,0,10,0));
	    pitchSlider.setBorder(
                BorderFactory.createEmptyBorder(0,0,10,0));

	    rollSlider.setMajorTickSpacing(90);
	    rollSlider.setMinorTickSpacing(10);
	    rollSlider.setPaintTicks(true);
	    rollSlider.setPaintLabels(true);
	    rollSlider.setBorder(
                BorderFactory.createEmptyBorder(0,0,10,0));

        
        //GridBagLayout 
	    myGridBagLayout = new GridBagLayout();
        // myGridLayout.
        //GridBagConstraints 
	    c = new GridBagConstraints();
        JPanel rightPane = new JPanel(myGridBagLayout);
        //JPanel rightPane = new JPanel(new BorderLayout());
        //rightPane.add(splitPane);
        
        //Put everything together.
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        rightPane.add(simulatorLabel, c);
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy++;
        rightPane.add(yawLabel, c);
        c.gridx = 1;
        rightPane.add(yawSlider, c);
        c.gridx = 0;
        c.gridy++;
        rightPane.add(pitchLabel, c);
        c.gridx = 1;
        rightPane.add(pitchSlider, c);
        c.gridx = 0;
        c.gridy++;
        rightPane.add(rollLabel, c);
        c.gridx = 1;
        rightPane.add(rollSlider, c);
        rightPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Now add a scrollable panel with more controls:
        JPanel settingsPane = new JPanel(new GridBagLayout());
        GridBagConstraints c2 = new GridBagConstraints();
        
        JScrollPane settingsScrollPane = new JScrollPane(settingsPane);
        settingsScrollPane.setVerticalScrollBarPolicy(
        		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        settingsScrollPane.setPreferredSize(new Dimension(250, 250));
        
        JLabel settingsLabel = new JLabel("Settings", JLabel.CENTER);
        //settingsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.anchor = GridBagConstraints.NORTHWEST;
        c2.gridwidth = 1;
        c2.gridx = 0;
        c2.gridy = 0;
        settingsPane.add(settingsLabel, c2);
        
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        c2.gridy++;
        settingsPane.add(separator, c2);
        
        ///////////////////////////////
        // Checkbox for 3 sensors
        JPanel supportedSensorsPane = new JPanel();
        supportedSensorsPane.setLayout(new BoxLayout(supportedSensorsPane, BoxLayout.PAGE_AXIS));
                
        supportedSensorsPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Supported sensors"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        
        mSupportedAccelerometer = new JCheckBox(ACCELEROMETER);
        mSupportedAccelerometer.setSelected(true);
        mSupportedAccelerometer.addItemListener(this);
        supportedSensorsPane.add(mSupportedAccelerometer);

        mSupportedCompass = new JCheckBox(COMPASS);
        mSupportedCompass.setSelected(true);
        mSupportedCompass.addItemListener(this);
        supportedSensorsPane.add(mSupportedCompass);

        mSupportedOrientation = new JCheckBox(ORIENTATION);
        mSupportedOrientation.setSelected(true);
        mSupportedOrientation.addItemListener(this);
        supportedSensorsPane.add(mSupportedOrientation);

        mSupportedThermometer = new JCheckBox(THERMOMETER);
        mSupportedThermometer.setSelected(false);
        mSupportedThermometer.addItemListener(this);
        supportedSensorsPane.add(mSupportedThermometer);

        c2.gridy++;
        settingsPane.add(supportedSensorsPane,c2);


        ///////////////////////////////
        // Checkbox for 3 sensors
        JPanel enabledSensorsPane = new JPanel();
        enabledSensorsPane.setLayout(new BoxLayout(enabledSensorsPane, BoxLayout.PAGE_AXIS));
                
        enabledSensorsPane.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("Enabled sensors"),
        BorderFactory.createEmptyBorder(5,5,5,5)));
        
        mEnabledAccelerometer = new JCheckBox(ACCELEROMETER);
        mEnabledAccelerometer.setSelected(true);
        mEnabledAccelerometer.addItemListener(this);
        enabledSensorsPane.add(mEnabledAccelerometer);

        mEnabledCompass = new JCheckBox(COMPASS);
        mEnabledCompass.setSelected(true);
        mEnabledCompass.addItemListener(this);
        enabledSensorsPane.add(mEnabledCompass);

        mEnabledOrientation = new JCheckBox(ORIENTATION);
        mEnabledOrientation.setSelected(true);
        mEnabledOrientation.addItemListener(this);
        enabledSensorsPane.add(mEnabledOrientation);

        mEnabledThermometer = new JCheckBox(THERMOMETER);
        mEnabledThermometer.setSelected(false);
        mEnabledThermometer.addItemListener(this);
        enabledSensorsPane.add(mEnabledThermometer);

        c2.gridy++;
        settingsPane.add(enabledSensorsPane,c2);
        

        JLabel label;
        GridBagConstraints c3;
        
        ////////////////////////////////
        // Gravity (in g = 9.81 m/s^2)
        JPanel gravityFieldPane = new JPanel(new GridBagLayout());
        c3 = new GridBagConstraints();
        c3.fill = GridBagConstraints.HORIZONTAL;
        c3.anchor = GridBagConstraints.NORTHWEST;
        c3.gridwidth = 3;
        c3.gridx = 0;
        c3.gridy = 0;
        
        gravityFieldPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Gravity"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        
        label = new JLabel("x: ", JLabel.LEFT);
        c3.gridwidth = 1;
        c3.gridx = 0;
        c3.gridy++;
        gravityFieldPane.add(label, c3);
        
        mGravityXText = new JTextField(5);
        mGravityXText.setText("0");
        c3.gridx = 1;
        gravityFieldPane.add(mGravityXText, c3);
        
        label = new JLabel(" g", JLabel.LEFT);
        c3.gridx = 2;
        gravityFieldPane.add(label, c3);
        
        
        label = new JLabel("y: ", JLabel.LEFT);
        c3.gridwidth = 1;
        c3.gridx = 0;
        c3.gridy++;
        gravityFieldPane.add(label, c3);
        
        mGravityYText = new JTextField(5);
        mGravityYText.setText("0");
        c3.gridx = 1;
        gravityFieldPane.add(mGravityYText, c3);
        
        label = new JLabel(" g", JLabel.LEFT);
        c3.gridx = 2;
        gravityFieldPane.add(label, c3);
        
        label = new JLabel("z: ", JLabel.LEFT);
        c3.gridwidth = 1;
        c3.gridx = 0;
        c3.gridy++;
        gravityFieldPane.add(label, c3);
        
        mGravityZText = new JTextField(5);
        mGravityZText.setText("-1");
        c3.gridx = 1;
        gravityFieldPane.add(mGravityZText, c3);
        
        label = new JLabel(" g", JLabel.LEFT);
        c3.gridx = 2;
        gravityFieldPane.add(label, c3);
        
        // Magnetic field panel ends
        
        // Add magnetic field panel to settings
        c2.gridx = 0;
        c2.gridwidth = 1;
        c2.gridy++;
        settingsPane.add(gravityFieldPane, c2);
        
        
        ////////////////////////////////
        // Magnetic field (in nanoTesla)
        
        // Values can be found at
        // 
        // Default values are for San Francisco.
        
        JPanel magneticFieldPane = new JPanel(new GridBagLayout());
        c3 = new GridBagConstraints();
        c3.fill = GridBagConstraints.HORIZONTAL;
        c3.anchor = GridBagConstraints.NORTHWEST;
        c3.gridwidth = 3;
        c3.gridx = 0;
        c3.gridy = 0;
        
        magneticFieldPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Magnetic field"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        
        JLabel magneticFieldNorthLabel = new JLabel("North component: ", JLabel.LEFT);
        c3.gridwidth = 1;
        c3.gridx = 0;
        c3.gridy++;
        magneticFieldPane.add(magneticFieldNorthLabel, c3);
        
        mMagneticFieldNorthText = new JTextField(5);
        mMagneticFieldNorthText.setText("22874.1");
        c3.gridx = 1;
        magneticFieldPane.add(mMagneticFieldNorthText, c3);
        
        JLabel nanoTeslaLabel = new JLabel(" nT", JLabel.LEFT);
        c3.gridx = 2;
        magneticFieldPane.add(nanoTeslaLabel, c3);
        
        
        JLabel magneticFieldEastLabel = new JLabel("East component: ", JLabel.LEFT);
        c3.gridwidth = 1;
        c3.gridx = 0;
        c3.gridy++;
        magneticFieldPane.add(magneticFieldEastLabel, c3);
        
        mMagneticFieldEastText = new JTextField(5);
        mMagneticFieldEastText.setText("5939.5");
        c3.gridx = 1;
        magneticFieldPane.add(mMagneticFieldEastText, c3);
        
        nanoTeslaLabel = new JLabel(" nT", JLabel.LEFT);
        c3.gridx = 2;
        magneticFieldPane.add(nanoTeslaLabel, c3);
        
        JLabel magneticFieldVerticalLabel = new JLabel("Vertical component: ", JLabel.LEFT);
        c3.gridwidth = 1;
        c3.gridx = 0;
        c3.gridy++;
        magneticFieldPane.add(magneticFieldVerticalLabel, c3);
        
        mMagneticFieldVerticalText = new JTextField(5);
        mMagneticFieldVerticalText.setText("43180.5");
        c3.gridx = 1;
        magneticFieldPane.add(mMagneticFieldVerticalText, c3);
        
        nanoTeslaLabel = new JLabel(" nT", JLabel.LEFT);
        c3.gridx = 2;
        magneticFieldPane.add(nanoTeslaLabel, c3);
        
        // Magnetic field panel ends
        
        // Add magnetic field panel to settings
        c2.gridx = 0;
        c2.gridwidth = 1;
        c2.gridy++;
        settingsPane.add(magneticFieldPane, c2);
        
        /////////////////////////////////////////////////////
        

        ////////////////////////////////
        // Temperature (in �C: Centigrade Celsius)
        JPanel temperatureFieldPane = new JPanel(new GridBagLayout());
        c3 = new GridBagConstraints();
        c3.fill = GridBagConstraints.HORIZONTAL;
        c3.anchor = GridBagConstraints.NORTHWEST;
        c3.gridwidth = 3;
        c3.gridx = 0;
        c3.gridy = 0;
        
        temperatureFieldPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Temperature"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        
        label = new JLabel("Temperature: ", JLabel.LEFT);
        c3.gridwidth = 1;
        c3.gridx = 0;
        c3.gridy++;
        temperatureFieldPane.add(label, c3);
        
        mTemperatureText = new JTextField(5);
        mTemperatureText.setText("17.7");
        c3.gridx = 1;
        temperatureFieldPane.add(mTemperatureText, c3);
        
        label = new JLabel(" �C", JLabel.LEFT);
        c3.gridx = 2;
        temperatureFieldPane.add(label, c3);
        
        
        // Temperature panel ends
        
        // Add temperature panel to settings
        c2.gridx = 0;
        c2.gridwidth = 1;
        c2.gridy++;
        settingsPane.add(temperatureFieldPane, c2);
        
        /////////////////////////////////////////////////////
        // Add settings scroll panel to right pane.
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy++;
        rightPane.add(settingsScrollPane, c);
        
        add(leftPane, BorderLayout.WEST);
        add(rightPane, BorderLayout.EAST);
        
        
        
        // Fill the possible values
        
        socketText.setText("8010");
        
        ipselectionText.append("Possible IP addresses:\n");
        try {
	        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
	        for (NetworkInterface netint : Collections.list(nets)) {
	            //out.printf("Display name: %s\n", netint.getDisplayName());
	            //ipselectionText.append("Name: " + netint.getName() + "\n");
	            //out.printf("Name: %s\n", netint.getName());
	            Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
	            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
	                //out.printf("InetAddress: %s\n", inetAddress);
	            	if (("" + inetAddress).compareTo("/127.0.0.1") != 0) {
	            		ipselectionText.append("" + inetAddress + "\n");
	            	}
	            	//ipselectionText.append("IP address: " + inetAddress + "\n");
		        }
	            //out.printf("\n");
	
	        }
        } catch (SocketException e) {
        	ipselectionText.append("Socket exception. Could not obtain IP addresses.");
        }

        
        
        // Set up the server:
        mSensorServer = new SensorServer(this);
        

        //Set up a timer that calls this object's action handler.
        delay = 500;
        timer = new Timer(delay, this);
        //timer.setInitialDelay(delay * 7); //We pause animation twice per cycle
                                          //by restarting the timer
        timer.setCoalesce(true);
        timer.setActionCommand(timerAction);
        
        timer.start();
        
	}
	

    /** Add a listener for window events. */
    void addWindowListener(Window w) {
        w.addWindowListener(this);
    }


    //React to window events.
    public void windowIconified(WindowEvent e) {
        timer.stop();
    }
    public void windowDeiconified(WindowEvent e) {
        timer.start();
    }
    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    
    
    /** Listen to the slider. */
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (source == yawSlider) {
        	mobile.yawDegree = source.getValue();
        	mobile.repaint();	
        } else if (source == pitchSlider) {
        	mobile.pitchDegree = source.getValue();
        	mobile.repaint();
        } else if (source == rollSlider) {
        	mobile.rollDegree = source.getValue();
        	mobile.repaint();
        }
    	
    	//if (!source.getValueIsAdjusting()) {
        //}
    }
    
    
    // Listener for checkbox events:
    // currently we don't have to do anything here...
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        /*
        if (source == mSupportedAccelerometer) {
        	
        } else if (source == mSupportedCompass) {
            
        }

        if (e.getStateChange() == ItemEvent.DESELECTED)
        
        */
    }
    


    //Called when the Timer fires, or selection is done
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        
        if (action.equals(yawPitch)) {
        	mouseMode = mouseYawPitch;
        } else if (action.equals(rollPitch)) {
        	mouseMode = mouseRollPitch;
        } else if (action.equals(move)) {
        	mouseMode = mouseMove;
        } else if (action.equals(timerAction)) {
        	doTimer();
        } else if (action.equals(setPortString)) {
        	setPort();
        }
    }
    
    public void doTimer() {
    	// Update acceleration:
    	mobile.updateMouseAcceleration();
    	
    	// Now show updated data:
    	showSensorData();
    }
    
    public void showSensorData() {
    	DecimalFormat mf = new DecimalFormat("#0.00");
		
    	String data = "";
    	// Accelerometer data
    	if (mSupportedAccelerometer.isSelected()) {
	    	data += ACCELEROMETER + ": ";
	    	if (mEnabledAccelerometer.isSelected()) {
				data += mf.format(mobile.accelx) + ", " 
						+ mf.format(mobile.accely) + ", "
						+ mf.format(mobile.accelz);
	    	} else {
				data += DISABLED;
	    	}
			data += "\n";
    	}
		
    	if (mSupportedCompass.isSelected()) {
    	    // Compass data
			data += COMPASS + ": ";
			if (mEnabledCompass.isSelected()) {
				data += mf.format(mobile.compassx) + ", " 
						+ mf.format(mobile.compassy) + ", "
						+ mf.format(mobile.compassz);
			} else {
				data += DISABLED;
			}
			data += "\n";
    	}
    	
		if (mSupportedOrientation.isSelected()) {
		    // Orientation data
			data += ORIENTATION + ": ";
			if (mEnabledOrientation.isSelected()) {
				data += mf.format(mobile.yaw) + ", " 
						+ mf.format(mobile.pitch) + ", "
						+ mf.format(mobile.roll);
			} else {
				data += DISABLED;
			}
			data += "\n";
		}
		
		if (mSupportedThermometer.isSelected()) {
			data += THERMOMETER + ": ";
			if (mEnabledThermometer.isSelected()) {
				data += mf.format(mobile.temperature);
			} else {
				data += DISABLED;
			}
			data += "\n";
		}
		// Output to textArea:
		textAreaSensorData.setText(data);
    }
    
    /**
     * Sets the socket port for listening
     */
    public void setPort() {
    	addMessage("Closing port " + mSensorServer.port);
    	// First close all old ports:
    	mSensorServer.stop();
    	
    	// now restart
    	mSensorServer = new SensorServer(this);
    }
    
    /**
     * Adds new message to message box.
     * If scroll position is at end, it will scroll to new message.
     * @param msg Message.
     */
    public void addMessage(String msg) {
    	
    	// from: http://forum.java.sun.com/thread.jspa?threadID=544890&tstart=0
    	// The following code fragments demonstrate how to auto scroll a JTextArea ("area") that's wrapped in a JScrollPane ("scrollPane"). Auto scrolling is enabled whenever the vertical scrollbar is located at the very bottom of the area. This allows you to scroll back up at leisure (disabling auto scroll) and then drag the vertical scrollbar back to the bottom to re-enable auto scroll:

    	// Determine whether the scrollbar is currently at the very bottom position.
    	JScrollBar vbar = areaScrollPane.getVerticalScrollBar();
    	final int tolerance = 10; // some tolerance value
    	boolean autoScroll = ((vbar.getValue() + vbar.getVisibleAmount() + tolerance) >= vbar.getMaximum());

    	// append to the JTextArea (that's wrapped in a JScrollPane named 'scrollPane'
    	ipselectionText.append(msg + "\n");

    	// now scroll if we were already at the bottom.
    	if( autoScroll ) ipselectionText.setCaretPosition( ipselectionText.getDocument().getLength() );    
    	 
    }
    
    /**
     * Get socket port number.
     * @return String containing port number.
     */
    public int getPort() {
    	String s = socketText.getText();
    	int port = 0;
    	try {
    		port = Integer.parseInt(s);
    	} catch (NumberFormatException e){
    		addMessage("Invalid port number: " + s);
    	}
    	return port;
    }
    
    /**
     * This method is called by SensorServerThread when
     * a new client connects.
     */
    public void newClient() {
    	mIncomingConnections++;
    	if (mIncomingConnections <= 1) {
    		// We have been connected for the first time.
    		// Disable all sensors:
    		mEnabledAccelerometer.setSelected(false);
    		mEnabledCompass.setSelected(false);
    		mEnabledOrientation.setSelected(false);
    		mEnabledThermometer.setSelected(false);
    		
    		addMessage("First incoming connection:");
    		addMessage("ALL SENSORS DISABLED!");
    	}
    }
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("SensorSimulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create the menu bar.  Make it have a green background.
        JMenuBar myMenuBar = new JMenuBar();
        myMenuBar.setPreferredSize(new Dimension(200, 20));

        //Create a yellow label to put in the content pane.
        JLabel yellowLabel = new JLabel();
        //yellowLabel.setOpaque(true);
        //yellowLabel.setBackground(new Color(248, 213, 131));
        yellowLabel.setPreferredSize(new Dimension(400, 180));

        //Start creating and adding components.
        JCheckBox changeButton =
                new JCheckBox("Glass pane \"visible\"");
        changeButton.setSelected(false);
        
        SensorSimulator simulator = new SensorSimulator();
        
        

        //Set the menu bar and add the label to the content pane.
        frame.setJMenuBar(myMenuBar);
        /*
        //Set up the content pane, where the "main GUI" lives.
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new FlowLayout());
        contentPane.add(changeButton);
        contentPane.add(new JButton("Button 1"));
        contentPane.add(new JButton("Button 2"));
        */
        
        //Add content to the window.
        frame.add(simulator, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}

