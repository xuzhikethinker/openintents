/*
 * Port of OpenIntents simulator to Android 2.1, extension to multi
 * emulator support, and GPS and battery simulation is developed as a
 * diploma thesis of Josip Balic at the University of Zagreb, Faculty of
 * Electrical Engineering and Computing.
 *
 * Copyright (C) 2008-2010 OpenIntents.org
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

/*
 * 09/Apr/08 Dale Thatcher <openintents at dalethatcher dot com>
 *           Added wii-mote data collection.
 */

package org.openintents.tools.simulator.view.sensor;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;

import org.openintents.tools.simulator.Global;
import org.openintents.tools.simulator.model.sensor.SensorSimulatorModel;
import org.openintents.tools.simulator.model.sensor.sensors.SensorModel;
import org.openintents.tools.simulator.view.sensor.sensors.AccelerometerView;
import org.openintents.tools.simulator.view.sensor.sensors.BarcodeReaderView;
import org.openintents.tools.simulator.view.sensor.sensors.LightView;
import org.openintents.tools.simulator.view.sensor.sensors.MagneticFieldView;
import org.openintents.tools.simulator.view.sensor.sensors.OrientationView;
import org.openintents.tools.simulator.view.sensor.sensors.ProximityView;
import org.openintents.tools.simulator.view.sensor.sensors.SensorView;
import org.openintents.tools.simulator.view.sensor.sensors.TemperatureView;
import org.openintents.tools.simulator.view.telnet.addons.ReplayAddonView;

/**
 * Class of SensorSimulator.
 * 
 * The SensorSimulator is a Java stand-alone application.
 * 
 * It simulates various sensors. An Android application can connect through
 * TCP/IP with the settings shown to the SensorSimulator to simulate
 * accelerometer, compass, orientation sensor, and thermometer.
 * 
 * @author Peli
 * @author Josip Balic
 */
public class SensorSimulatorView extends JPanel {
	private static final long serialVersionUID = -587503580193069930L;

	// port for sensor simulation
	private JTextField sensorPortText;
	private JButton sensorPortButton;

	// Field for socket related output:
	// private JScrollPane areaScrollPane;
	private JTextArea messageTextArea;

	// Field for sensor simulator data output:
	private JTextArea textAreaSensorData;

	// Settings
	private JTextField mUpdateText;
	private JTextField mRefreshCountText;
	private JLabel mRefreshSensorsLabel;

	// Displays the mobile phone
	private DeviceView mobile;

	// Server for sending out sensor data

	private SensorSimulatorModel model;
	private ArrayList<SensorView> sensors;
	private ReplayAddonView replayAddonView;

	private JPanel enabledSensorsPane;

	private CompoundBorder enabledBorder;

	private CompoundBorder disabledBorder;

	public SensorSimulatorView(SensorSimulatorModel model) {
		this.model = model;
		// sensors
		sensors = new ArrayList<SensorView>();
		sensors.add(new AccelerometerView(model.getAccelerometer()));
		sensors.add(new MagneticFieldView(model.getMagneticField()));
		sensors.add(new OrientationView(model.getOrientation()));
		sensors.add(new TemperatureView(model.getTemperature()));
		sensors.add(new BarcodeReaderView(model.getBarcodeReader()));
		sensors.add(new LightView(model.getLight()));
		sensors.add(new ProximityView(model.getProximity()));

		replayAddonView = new ReplayAddonView(model.getReplayAddon());

		enabledBorder = BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Enabled sensors"),
				BorderFactory
						.createMatteBorder(5, 5, 5, 5, Global.BORDER_COLOR));
		disabledBorder = BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Enabled sensors - readonly"),
				BorderFactory
						.createMatteBorder(5, 5, 5, 5, Global.NOTIFY_COLOR));
		
		// up/down & split
		JPanel upPanel = fillUpPanel();
		JPanel downPanel = fillDownPanel();

		JSplitPane splitPaneVertical = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT, upPanel, downPanel);
		splitPaneVertical.setResizeWeight(Global.SENSOR_SPLIT_UP);
		add(splitPaneVertical);
	}

	private JPanel fillDownPanel() {
		JPanel downPanel = new JPanel(new GridBagLayout());

		GridBagConstraints layout = new GridBagConstraints();
		layout.fill = GridBagConstraints.VERTICAL;
		layout.gridx = 0;
		layout.gridy = 0;
		// Sensor output update/measure frequency
		JPanel updateSimulationPanel = fillUpdateSimulationPanel();
		downPanel.add(updateSimulationPanel, layout);

		layout.gridx++;
		layout.gridy = 0;
		// Replay Pane
		JPanel replayFieldPane = new JPanel(new GridBagLayout());
		replayFieldPane.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Replay"),
				BorderFactory.createEmptyBorder(0, 0, 0, 0)));
		replayAddonView.fillPane(replayFieldPane);
		downPanel.add(replayFieldPane, layout);

		layout.gridx++;
		layout.gridy = 0;
		// info output
		JScrollPane areaScrollPane = fillInfoOutput();
		downPanel.add(areaScrollPane, layout);

		layout.gridx++;
		layout.gridy = 0;
		// sensors log/values
		textAreaSensorData = new JTextArea(3, 100);
		JScrollPane scrollPaneSensorData = new JScrollPane(textAreaSensorData);
		scrollPaneSensorData
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPaneSensorData.setPreferredSize(new Dimension(250, 80));
		downPanel.add(scrollPaneSensorData, layout);

		return downPanel;
	}

	private JScrollPane fillInfoOutput() {
		messageTextArea = new JTextArea(3, 10);
		messageTextArea.append("Write emulator command port and\n"
				+ "click on set to create connection.\n");
		messageTextArea.append("Possible IP addresses:\n");
		try {
			Enumeration<NetworkInterface> nets = NetworkInterface
					.getNetworkInterfaces();
			for (NetworkInterface netint : Collections.list(nets)) {
				Enumeration<InetAddress> inetAddresses = netint
						.getInetAddresses();
				for (InetAddress inetAddress : Collections.list(inetAddresses)) {
					if (("" + inetAddress).compareTo("/127.0.0.1") != 0) {
						messageTextArea.append("" + inetAddress + "\n");
					}
				}
			}
		} catch (SocketException e) {
			messageTextArea
					.append("Socket exception. Could not obtain IP addresses.");
		}

		JScrollPane areaScrollPane = new JScrollPane(messageTextArea);
		areaScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		areaScrollPane.setPreferredSize(new Dimension(250, 80));
		return areaScrollPane;
	}

	private JPanel fillUpdateSimulationPanel() {
		JPanel updateSimulationPanel = new JPanel(new GridBagLayout());
		GridBagConstraints layout = new GridBagConstraints();
		updateSimulationPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Simulation update"),
				BorderFactory.createEmptyBorder(0, 0, 0, 0)));

		// Update sensors
		layout.gridy = 0;
		layout.gridx = 0;
		layout.fill = GridBagConstraints.HORIZONTAL;
		layout.anchor = GridBagConstraints.NORTHWEST;
		JLabel label = new JLabel("Update sensors: ", JLabel.LEFT);
		updateSimulationPanel.add(label, layout);

		mUpdateText = new JTextField(5);
		mUpdateText.setText("" + model.getUpdateSensors());
		layout.gridx++;
		updateSimulationPanel.add(mUpdateText, layout);

		label = new JLabel(" ms", JLabel.LEFT);
		layout.gridx++;
		updateSimulationPanel.add(label, layout);

		// Refresh after
		layout.gridy++;
		label = new JLabel("Refresh after: ", JLabel.LEFT);
		layout.gridx = 0;
		updateSimulationPanel.add(label, layout);

		mRefreshCountText = new JTextField(5);
		mRefreshCountText.setText("10");
		layout.gridx++;
		updateSimulationPanel.add(mRefreshCountText, layout);

		label = new JLabel(" times", JLabel.LEFT);
		layout.gridx++;
		updateSimulationPanel.add(label, layout);

		// Sensor update
		layout.gridy++;
		label = new JLabel("Sensor update: ", JLabel.LEFT);
		layout.gridx = 0;
		updateSimulationPanel.add(label, layout);

		mRefreshSensorsLabel = new JLabel("0", JLabel.LEFT);
		layout.gridx++;
		updateSimulationPanel.add(mRefreshSensorsLabel, layout);

		// Emulator update
		layout.gridy++;
		label = new JLabel("Emulator update: ", JLabel.LEFT);
		layout.gridx = 0;
		updateSimulationPanel.add(label, layout);

		return updateSimulationPanel;
	}

	private JPanel fillUpPanel() {
		JPanel upPanel = new JPanel(new BorderLayout());

		JScrollPane leftScrollPane = fillLeftPanel();
		JScrollPane rightScrollPane = fillRightPanel();

		JSplitPane splitUpPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftScrollPane, rightScrollPane);
		splitUpPane.setResizeWeight(Global.SENSOR_SPLIT_LEFT);

		upPanel.add(splitUpPane);
		return upPanel;
	}

	private JScrollPane fillRightPanel() {
		JPanel rightPanel = new JPanel(new GridBagLayout());
		JScrollPane rightScrollPane = new JScrollPane(rightPanel);
		rightScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		rightScrollPane.setPreferredSize(new Dimension(
				(int) (Global.WIDTH * Global.SENSOR_SPLIT_RIGHT),
				(int) (Global.HEIGHT * Global.SENSOR_SPLIT_UP)));
		GridBagConstraints layout = new GridBagConstraints();
		layout.gridx = 0;
		layout.gridy = 0;
		layout.fill = GridBagConstraints.HORIZONTAL;
		layout.anchor = GridBagConstraints.NORTHWEST;
		for (SensorView sensor : sensors) {
			rightPanel.add(sensor, layout);
			layout.gridy++;
		}
		return rightScrollPane;
	}

	private JScrollPane fillLeftPanel() {
		JPanel leftPanel = new JPanel(new GridBagLayout());
		JScrollPane leftScrollPane = new JScrollPane(leftPanel);
		leftScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		GridBagConstraints layout = new GridBagConstraints();

		layout.gridx = 0;
		layout.gridy = 0;
		// Add IP address properties:
		Font fontNotify = new Font("SansSerif", Font.BOLD, 12);
		sensorPortText = new JTextField(5);
		sensorPortText.setText("" + model.getSimulationPort());
		JPanel ipPanel = new JPanel(new GridLayout(1, 0));
		JLabel socketLabel = new JLabel("Socket  ", JLabel.LEFT);
		socketLabel.setFont(fontNotify);
		socketLabel.setForeground(Global.NOTIFY_COLOR);
		ipPanel.add(socketLabel, layout);
		ipPanel.add(sensorPortText, layout);
		sensorPortButton = new JButton("Set");
		sensorPortButton.setFont(fontNotify);
		sensorPortButton.setForeground(Global.NOTIFY_COLOR);
		ipPanel.add(sensorPortButton);
		leftPanel.add(ipPanel, layout);

		layout.gridx = 0;
		layout.gridy = 1;
		
		// Enabled Sensors
		enabledSensorsPane = new JPanel();
		enabledSensorsPane.setLayout(new GridLayout(0, 2));
		enabledSensorsPane.setBorder(enabledBorder);
		for (SensorView sensor : sensors) {
			sensor.addEnable(enabledSensorsPane);
		}
		leftPanel.add(enabledSensorsPane, layout);

		layout.gridx = 0;
		layout.gridy = 2;
		// Add the mobile
		mobile = new DeviceView(model);
		leftPanel.add(mobile, layout);

		return leftScrollPane;
	}

	/**
	 * Sets the socket port for listening
	 */
	public void setPort() {
		addMessage("Closing port " + model.getSimulationPort());
		model.restartSensorServer();
	}

	/**
	 * Adds new message to message box. If scroll position is at end, it will
	 * scroll to new message.
	 * 
	 * @param msg
	 *            Message.
	 */
	public void addMessage(String msg) {

		// // Determine whether the scrollbar is currently at the very bottom
		// // position.
		// JScrollBar vbar = areaScrollPane.getVerticalScrollBar();
		// final int tolerance = 10; // some tolerance value
		// boolean autoScroll = ((vbar.getValue() + vbar.getVisibleAmount() +
		// tolerance) >= vbar
		// .getMaximum());
		//
		// // append to the JTextArea (that's wrapped in a JScrollPane named
		// // 'scrollPane'
		messageTextArea.append(msg + "\n");

		// now scroll if we were already at the bottom.
		// if (autoScroll)
		// ipselectionText.setCaretPosition(ipselectionText.getDocument()
		// .getLength());

	}

	/**
	 * Get socket port number.
	 * 
	 * @return String containing port number.
	 */
	public int getPort() {
		String s = sensorPortText.getText();
		int port = 0;
		try {
			port = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			addMessage("Invalid port number: " + s);
		}
		return port;
	}

	/**
	 * This method is called by SensorServerThread when a new client connects.
	 */
	public void newClient() {
		addMessage("First incoming connection:");
		addMessage("ALL SENSORS DISABLED!");
	}

	/**
	 * Safely retries the double value of a text field. If the value is not a
	 * valid number, 0 is returned, and the field is marked red.
	 * 
	 * @param textfield
	 *            Textfield from which the value should be read.
	 * @param defaultValue
	 *            default value if input field is invalid.
	 * @return double value.
	 */
	public double getSafeDouble(JTextField textfield, double defaultValue) {
		double value = defaultValue;

		try {
			value = Double.parseDouble(textfield.getText());
			textfield.setBackground(Color.WHITE);
		} catch (NumberFormatException e) {
			// wrong user input in box - take default values.
			value = defaultValue;
			textfield.setBackground(Color.RED);
		}
		return value;
	}

	public double getSafeDouble(JTextField textfield) {
		return getSafeDouble(textfield, 0);
	}

	/**
	 * Safely retries the float value of a text field. If the value is not a
	 * valid number, 0 is returned, and the field is marked red.
	 * 
	 * @param textfield
	 *            TextField from which the value should be read.
	 * @param defaultValue
	 *            default value if input field is invalid.
	 * @return float value.
	 */
	public float getSafeFloat(JTextField textfield, float defaultValue) {
		float value = defaultValue;
		try {
			value = Float.parseFloat(textfield.getText());
			textfield.setBackground(Color.WHITE);
		} catch (NumberFormatException e) {
			// wrong user input in box - take default values.
			value = defaultValue;
			textfield.setBackground(Color.RED);
		}
		return value;
	}

	public float getSafeFloat(JTextField textfield) {
		return getSafeFloat(textfield, 0);
	}

	/**
	 * Safely retries the a list of double values of a text field. If the list
	 * contains errors, null is returned, and the field is marked red.
	 * 
	 * @param textfield
	 *            TextField from which the value should be read.
	 * @return list double[] with values or null.
	 */
	public double[] getSafeDoubleList(JTextField textfield) {
		double[] valuelist = null;

		try {
			String t = textfield.getText();
			// Now we have to split this into pieces
			String[] tlist = t.split(",");
			int len = tlist.length;
			if (len > 0) {
				valuelist = new double[len];
				for (int i = 0; i < len; i++) {
					valuelist[i] = Double.parseDouble(tlist[i]);
				}
			} else {
				valuelist = null;
			}
			textfield.setBackground(Color.WHITE);
		} catch (NumberFormatException e) {
			// wrong user input in box - take default values.
			valuelist = null;
			textfield.setBackground(Color.RED);
		}
		return valuelist;
	}

	//
	// /**
	// * is called from within doTimer() to record/playback values
	// * recording/playback is triggered from actionListener
	// */
	// private void updateFromFile() {
	// OrientationModel orientation = (OrientationModel) sensors
	// .get(SensorModel.POZ_ORIENTATION);
	//
	// replayAddon.recordData(orientation.getReadYaw(),
	// orientation.getReadRoll(), orientation.getReadPitch());
	//
	// if (replayAddon.playData()) {
	// // Update sliders
	// orientation.setYawSlider(replayAddon.getYaw());
	// orientation.setRollSlider(replayAddon.getRoll());
	// orientation.setPitchSlider(replayAddon.getPitch());
	// mobile.doRepaint();
	// } else {
	// replayAddonView.setPlaybackText("Playback");
	// }
	// }

	// ///////////////////////////////
	// implements SensorSimulatorModel
	// ///////////////////////////////

	public double getUpdateSensors() {
		return getSafeDouble(mUpdateText);
	}

	public double getRefreshAfter() {
		return getSafeDouble(mRefreshCountText);
	}

	public BarcodeReaderView getBarcodeReader() {
		return (BarcodeReaderView) sensors.get(SensorModel.POZ_BARCODE_READER);
	}

	public AccelerometerView getAccelerometer() {
		return (AccelerometerView) sensors.get(SensorModel.POZ_ACCELEROMETER);
	}

	public LightView getLight() {
		return (LightView) sensors.get(SensorModel.POZ_LIGHT);
	}

	public OrientationView getOrientation() {
		return (OrientationView) sensors.get(SensorModel.POZ_ORIENTATION);
	}

	public ProximityView getProximity() {
		return (ProximityView) sensors.get(SensorModel.POZ_PROXIMITY);
	}

	public TemperatureView getTemperature() {
		return (TemperatureView) sensors.get(SensorModel.POZ_TEMPERATURE);
	}

	public MagneticFieldView getMagneticField() {
		return (MagneticFieldView) sensors.get(SensorModel.POZ_MAGNETIC_FIELD);
	}

	public JButton getSensorPortButton() {
		return sensorPortButton;
	}

	public DeviceView getDevice() {
		return mobile;
	}

	public ReplayAddonView getReplayAddon() {
		return replayAddonView;
	}

	public long getRefreshCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setRefreshSensorsLabel(double ms) {
		mRefreshSensorsLabel.setText(Global.TWO_DECIMAL_FORMAT.format(ms)
				+ " ms");
	}

	public void setOutput(String data) {
		textAreaSensorData.setText(data);
	}

	public JTextArea getMessagePanel() {
		return messageTextArea;
	}

	public void fixEnabledSensors() {
		enabledSensorsPane.setBorder(disabledBorder);
	}

	public void unfixEnabledSensors() {
		enabledSensorsPane.setBorder(enabledBorder);
	}

}