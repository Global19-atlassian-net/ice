/*******************************************************************************
 * Copyright (c) 2014 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - Jay Jay Billings,
 *   Jordan H. Deyton, Dasha Gorin, Alexander J. McCaskey, Taylor Patterson,
 *   Claire Saunders, Matthew Wang, Anna Wojtowicz
 *******************************************************************************/
package org.eclipse.ice.io.ips;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.ice.datastructures.form.AllowedValueType;
import org.eclipse.ice.datastructures.form.DataComponent;
import org.eclipse.ice.datastructures.form.Entry;
import org.eclipse.ice.datastructures.form.Form;
import org.eclipse.ice.datastructures.form.MasterDetailsComponent;
import org.eclipse.ice.datastructures.form.TableComponent;
import org.eclipse.ice.io.serializable.IReader;

/**
 * <p>
 * IPSReader class is responsible for reading the contents of an IPS INI (.conf)
 * file and converting it into appropriate data structures for ICE to use.
 * </p>
 * 
 * @author Andrew Bennett
 * 
 */
public class IPSReader implements IReader {

	/**
	 * <p>
	 * Keeps track of the current ID for entries. Since we don't know how many
	 * entries we will need for each section we can just keep a global count of
	 * them.
	 * <p>
	 */
	private int currID = 0;

	/**
	 * Nullary constructor
	 */
	public IPSReader() {
		super();
		return;
	}

	/**
	 * <p>
	 * Reads in the given IFile to the CaebatModel datastructures. There are
	 * four components in each IPS INI file that are parsed and arranged into
	 * the form that will be displayed to the user. Returns a form composed of
	 * the data read in from the specified location which can be modified and
	 * written out by the user.
	 * </p>
	 * 
	 * @param ifile
	 *            The file to be read in
	 * @return a form with the imported data
	 */
	@Override
	public Form read(IFile ifile) {
		// Make sure there's something to look in
		if (ifile == null) {
			return null;
		}
		Form form = new Form();

		// Read in the ini file to an ArrayList<String>
		ArrayList<String> lines = null;
		try {
			lines = readFileLines(ifile);
		} catch (FileNotFoundException e) {
			System.out
					.println("IPSReader Message: Error!  Could not find file for loading.");
			return null;
		} catch (IOException e) {
			System.out
					.println("IPSReader Message: Error!  Trouble reading file.");
			return null;
		}

		// Get an iterator over the input to pass to the loading methods
		Iterator<String> iniIterator = lines.iterator();

		// Read in the global configuration and ports data
		TableComponent globalConfiguration = loadGlobalConfiguration(iniIterator);
		TableComponent portsData = loadPortsData(iniIterator);

		// Determine the number of components that were specified in
		// the ports table and then read them in
		int numberPorts = portsData.numberOfRows();
		ArrayList<DataComponent> ipsComponents = new ArrayList<DataComponent>();
		ArrayList<String> names = new ArrayList<String>();

		// Read in each of the ports individually
		for (int i = 0; i < numberPorts; i++) {
			DataComponent ipsComponent = loadComponent(iniIterator);
			ipsComponents.add(ipsComponent);
			names.add(ipsComponent.getName());
		}

		// Build a MasterDetailsComponent out of the DataComponents
		MasterDetailsComponent portsMaster = buildMasterDetailsComponent(ipsComponents);

		// Read in the time loop specification
		DataComponent timeLoopComponent = loadTimeLoopComponent(iniIterator);

		// Add the components to the form
		form.addComponent(timeLoopComponent);
		form.addComponent(globalConfiguration);
		form.addComponent(portsData);
		form.addComponent(portsMaster);

		// Return the form
		return form;
	}

	/**
	 * <p>
	 * Searches a given IFile for content that matches a given regular
	 * expression. Returns all instances that match.
	 * </p>
	 * 
	 * @param ifile
	 *            The file to search in
	 * @param regex
	 *            A string representing a regular expression containing the
	 *            specification of what to search for
	 * @return an ArrayList<Entry> with the description set to the regex and the
	 *         value set to the results.
	 */
	@Override
	public ArrayList<Entry> findAll(IFile ifile, String regex) {
		// Make sure there's something to look in
		if (ifile == null) {
			System.out.println("IPSReader Message: Error!  Null file given.");
			return null;
		}

		// Read in the ini file and create the iterator
		Entry foundEntry;
		ArrayList<Entry> matchedEntries = new ArrayList<Entry>();
		ArrayList<String> lines = null;
		try {
			lines = readFileLines(ifile);
		} catch (FileNotFoundException e) {
			System.out
					.println("IPSReader Message: Error!  Could not find file for loading.");
			return null;
		} catch (IOException e) {
			System.out
					.println("IPSReader Message: Error!  Trouble reading file.");
			return null;
		}

		// Go through each line and look for matches
		for (String line : lines) {
			if (line.matches(regex)) {
				foundEntry = makeIPSEntry();
				foundEntry.setName(line);
				foundEntry.setDescription(regex);
				foundEntry.setValue(line);
				matchedEntries.add(foundEntry);
			}
		}

		return matchedEntries;
	}

	/**
	 * <p>
	 * Returns a string saying this is an IPSReader
	 * </p>
	 * 
	 * @return the type of reader
	 */
	public String getReaderType() {
		return "IPSReader";
	}

	/**
	 * Read the INI file lines into entries of an ArrayList so that it is easy
	 * to parse the sections into ICE.
	 * 
	 * @param iniFile
	 *            The INI file to be read in.
	 * @return An ArrayList of strings of the lines of the INI file.
	 * @throws FileNotFoundException
	 *             Thrown when the INI file cannot be found.
	 * @throws IOException
	 *             Thrown when the INI file cannot be read or closed.
	 */
	private ArrayList<String> readFileLines(IFile ifile)
			throws FileNotFoundException, IOException {

		// Read in the ini file and create the iterator
		File file = new File(ifile.getFullPath().toOSString());
		BufferedReader reader = new BufferedReader(new FileReader(file));

		// Read the FileInputStream and append to a StringBuffer
		StringBuffer buffer = new StringBuffer();
		int fileByte;
		while ((fileByte = reader.read()) != -1) {
			buffer.append((char) fileByte);
		}
		reader.close();

		// Break up the StringBuffer at each newline character
		String[] bufferSplit = (buffer.toString()).split("\n");
		ArrayList<String> fileLines = new ArrayList<String>(
				Arrays.asList(bufferSplit));

		// Add a dummy EOF line so that the last line of the file is
		// read in correctly
		fileLines.add("EOF");

		// Return the ArrayList
		return fileLines;
	}

	/**
	 * Loads the top section of the IPS framework INI file and returns the
	 * contents as a DataComponent of Entries. Each line is set as an Entry.
	 * 
	 * @param it
	 *            Iterator over the lines of the INI file as an ArrayList
	 * @return A TableComponent of Entries representing the contents at the top
	 *         of the INI file.
	 */
	private TableComponent loadGlobalConfiguration(Iterator<String> it) {
		// Create the global configuration component
		TableComponent globalConfiguration = new TableComponent();
		globalConfiguration.setName("Global Configuration");
		globalConfiguration.setDescription("Entries at the top of an "
				+ "IPS framework INI input file.");
		globalConfiguration.setId(currID);
		currID++;
		Entry entry;
		String[] splitLine = null;

		// Build the template for the ports table
		ArrayList<Entry> entries = new ArrayList<Entry>();
		Entry portNameTemplate = makeIPSEntry();
		Entry implementationTemplate = makeIPSEntry();
		portNameTemplate.setName("Name");
		implementationTemplate.setName("Value");
		entries.add(portNameTemplate);
		entries.add(implementationTemplate);
		globalConfiguration.setRowTemplate(entries);

		// Read in new parameters until we reach the PORTS entry or the end
		// while only taking in lines that have variable assignments
		String line = it.next();
		while (!line.contains("[PORTS]") && it.hasNext()) {

			// The format in this section is: KEY = VALUE # Comment
			// First check if the line contains a parameter
			if (line.contains("=")) {

				// If the line has a comment split on it and disregard it
				if (line.contains("#")) {
					line = line.split("#", 2)[0];
				}

				// Get the content for the entry
				splitLine = line.split("=", 2);

				// Set up the data in the table
				ArrayList<Entry> row = new ArrayList<Entry>();
				int rowID = globalConfiguration.addRow();
				row = globalConfiguration.getRow(rowID);
				row.get(0).setValue(splitLine[0]);
				row.get(1).setValue(splitLine[1]);
			}

			// Read in another line
			line = it.next();
		}

		// Make sure that we are not at the end of the file
		if (!it.hasNext()) {
			System.err.println("IPS Reader Message: Reached unexpected "
					+ "end of file while reading the global configuration.");
			return null;
		}

		// Return the parameters
		return globalConfiguration;
	}

	/**
	 * Loads the Ports table from the INI file and returns the information as a
	 * DataComponent of Entry. Each of the ports specified the table are set as
	 * an entry. The number of ports in the table specify the number of
	 * Components that need to be loaded by the loadComponent() method.
	 * 
	 * @param it
	 *            Iterator over the lines of the INI file as an ArrayList
	 * @return A TableComponent of Entries representing the contents in the
	 *         ports table of the INI file.
	 */
	private TableComponent loadPortsData(Iterator<String> it) {

		// Create the ports component
		TableComponent portsTable = new TableComponent();
		portsTable.setName("Ports Table");
		portsTable.setDescription("Entries in the Ports table of "
				+ "an IPS framework INI input file");
		portsTable.setId(currID);
		currID++;

		// Build the template for the ports table
		ArrayList<Entry> entries = new ArrayList<Entry>();
		Entry portNameTemplate = makeIPSEntry();
		Entry implementationTemplate = makeIPSEntry();
		portNameTemplate.setName("Port Name");
		implementationTemplate.setName("Implementation");
		entries.add(portNameTemplate);
		entries.add(implementationTemplate);
		portsTable.setRowTemplate(entries);

		// Make sure that the file keeps going. After the completion of the
		// loadGlobalConfiguration() method the current line that the iterator
		// is at should be [PORTS]
		if (!it.hasNext()) {
			System.err
					.println("Reached unexpected end of file while trying to "
							+ "locate the Ports Table.  Please check your input file and "
							+ "try again.");
			return null;
		}

		// Iterate until we get to the entries in the Ports Table. The first
		// entry is an enumeration of the ports that will follow. We need to
		// record those before going on so that we can verify that all of the
		// ports are declared correctly.
		String line = it.next();
		while (it.hasNext() && !line.contains("NAMES = ")) {
			line = it.next();
		}

		// There should be more to the file after the port names has been read
		// in, so make sure that we've not accidentally parsed over the entire
		// thing. If we have, tell the user that their file may be incorrect.
		if (!it.hasNext()) {
			System.err
					.println("Reached unexpected end of file while trying to "
							+ "read the Ports Table.  "
							+ "Please check your input file and try again.");
			return null;
		}

		// Get the names specified in the NAMES entry by splitting on the =
		// sign and then keeping everything after, which we then split on each
		// space, and turn that into an ArrayList for easier searching later
		ArrayList<String> portNames = new ArrayList<String>(Arrays.asList(line
				.split(" = ")[1].split(" ")));

		// Go through the rest of the ports table and add the entries as we
		// find them, while making sure that we find all of them.
		while (portNames.size() > 0 && it.hasNext()) {

			// Check if this line contains an entry
			if (line.contains("[[") && line.contains("]]")) {

				// Take care of comments
				if (line.contains("#")) {
					line = line.split("#", 2)[0];
				}

				// Get the port name of the entry & make sure that it is in
				// the list of portNames.
				int rowID = portsTable.addRow();
				ArrayList<Entry> row = portsTable.getRow(rowID);

				String portName = line.replaceAll("[^a-zA-Z0-9_]", "");
				if (portNames.contains(portName)) {
					// Set the details for the entry
					row.get(0).setValue(portName);

					// The next line should give details of the implementation
					line = it.next();
					if (line.contains("#")) {
						line = line.split("#", 2)[0];
					}

					// See if the information we are looking for is there
					if (line.contains("IMPLEMENTATION = ")) {
						String implementation = line.split(" = ", 2)[1];
						row.get(1).setValue(implementation);
					} else {
						System.err
								.println("Unexpected token after ports entry, "
										+ "check your input file and try again.");
						System.exit(1);
					}

					// Found a complete port entry, now remove the port from the
					// list of remaining ports to be found.
					portNames.remove(portName);
				}
			}
			// read another line
			line = it.next();
		}

		// Make sure that we are not at the end of the file
		if (!it.hasNext()) {
			System.err.println("IPS Reader Message: Reached unexpected "
					+ "end of file while reading the ports table.");
			return null;
		}

		return portsTable;
	}

	/**
	 * Load the data for a component that was listed in the ports table and
	 * return the information as a DataComponent. Each parameter in the
	 * component is added to the DataComponent as an entry.
	 * 
	 * @param it
	 *            Iterator over the lines of the INI file as an ArrayList
	 * @return A DataComponent containing information for one of the port
	 *         entries.
	 */
	private DataComponent loadComponent(Iterator<String> it) {
		// Create the port component and a generic entry
		DataComponent portComponent = new DataComponent();
		Entry entry;
		String[] splitLine = null;

		// Scan until we get to the next port component
		String line = it.next();
		while (!line.contains("[") && !line.contains("]") && it.hasNext()) {
			line = it.next();
		}

		// Pull the port name and start parsing through the parameters
		String portName = line.replaceAll("[^a-zA-Z0-9_]", "");
		portComponent.setName(portName);
		portComponent.setDescription("A port in an IPS file.");
		portComponent.setId(currID);
		currID++;

		// Read parameters until reaching a whitespace line that separates ports
		while (line.trim().length() > 0) {
			// The format in this section is: KEY = VALUE # Comment
			// First check if the line contains a parameter
			if (line.contains("=")) {

				// If the line has a comment split on it and disregard it
				if (line.contains("#")) {
					line = line.split("#", 2)[0];
				}

				// Get the content for the entry
				splitLine = line.split("=", 2);

				// Set up the entry
				entry = makeIPSEntry();
				entry.setName(splitLine[0]);
				entry.setValue(splitLine[1]);
				entry.setId(currID);
				currID++;
				portComponent.addEntry(entry);
			}

			// Read in another line
			line = it.next();
		}
		// Make sure that we are not at the end of the file
		if (!it.hasNext()) {
			System.err.println("IPS Reader Message: Reached unexpected "
					+ "end of file while reading the port configuration.");
			return null;
		}
		// Return the parameters
		return portComponent;
	}

	/**
	 * <p>
	 * Constructs the MasterDetailsComponent that allows the user to add and
	 * delete Ports entries. Supplies some limitations on the types of ports
	 * that are allowed in the MasterDetailsComponent that aligns with the IPS
	 * framework.
	 * </p>
	 * 
	 * @param ipsComponents
	 *            The DataComponents to be put into the MasterDetailsComponent
	 * @return the resultant MasterDetailsComponent
	 */
	private MasterDetailsComponent buildMasterDetailsComponent(
			ArrayList<DataComponent> ipsComponents) {
		MasterDetailsComponent masterDetails = new MasterDetailsComponent();
		masterDetails.setName("Ports Master");
		masterDetails
				.setDescription("Setup for each of the ports in the simulation.");
		String portName;
		int masterId;

		// Set the allowed ports so that users don't try to go too far and end
		// up with settings that don't exist
		String[] allowedPortNames = { "INIT", "INIT_STATE", "AMPERES_THERMAL",
				"AMPERES_ELECTRICAL", "CANTR",
				"CHARTRAN_ELECTRICAL_THERMAL_DRIVER", "NTG", "DUALFOIL",
				"CUBIT_MESHGEN", "MESHGEN_CHARTRAN_ELECTRICAL_THERMAL_DRIVER" };
		ArrayList<String> allowedPortList = new ArrayList<String>(
				Arrays.asList(allowedPortNames));
		ArrayList<DataComponent> portTemplates = new ArrayList<DataComponent>();
		Boolean portAdded;

		// Set up the master details template for all available ports
		for (String port : allowedPortList) {
			portAdded = false;
			// Check to see if the port was in the file and use the
			// correct data component if it was.
			for (int i = 0; i < ipsComponents.size(); i++) {
				DataComponent data = ipsComponents.get(i);
				if (port.equals(data.getName())) {
					portTemplates.add(data);
					portAdded = true;
				}
			}
			// If it wasn't in the file then just add it with a basic
			// template.
			if (!portAdded) {
				portTemplates.add(ipsComponents.get(0));
			}
		}

		// Set the template and add a dummy for indexing purposes
		masterDetails.setTemplates(allowedPortList, portTemplates);
		masterId = masterDetails.addMaster();
		// Add the ports to the MasterDetailsComponent
		for (DataComponent data : ipsComponents) {
			portName = data.getName();
			masterId = masterDetails.addMaster();
			masterDetails.getDetailsAtIndex(masterId - 1).copy(data);
			masterDetails.setMasterInstanceValue(masterId - 1, portName);

		}
		// Delete the first dummy master that was added so that
		// the details are correct.
		masterDetails.deleteMaster(masterDetails.numberOfMasters());

		return masterDetails;
	}

	/**
	 * Load the Time Loop Data from the INI file and return the data as a
	 * DataComponent. Each parameter in the section is added to the
	 * DataComponent as an Entry.
	 * 
	 * @param it
	 *            Iterator over the lines of the INI file as an ArrayList
	 * @return a DataComponent of Entries containing the information from the
	 *         Time Loop Component of the INI file.
	 */
	private DataComponent loadTimeLoopComponent(Iterator<String> it) {
		// Create the port component and a generic entry
		DataComponent timeLoopData = new DataComponent();
		Entry entry;
		String[] splitLine = null;
		String line = "";

		// Scan until we get to the next port component
		if (it.hasNext()) {
			line = it.next();
		} else {
			System.err.println("Unexpectedly reached the end of file.  "
					+ "Please check the INI file you have chosen and"
					+ " try again.");
			return null;
		}

		while (!line.contains("[TIME_LOOP]") && it.hasNext()) {
			line = it.next();
		}

		// Pull the port name and start parsing through the parameters
		timeLoopData.setName("Time Loop Data");
		timeLoopData.setDescription("");
		timeLoopData.setId(currID);
		currID++;

		// Read parameters until reaching the end of the file
		while (it.hasNext()) {
			// The format in this section is: KEY = VALUE # Comment
			// First check if the line contains a parameter
			if (line.contains("=")) {

				// If the line has a comment split on it and disregard it
				if (line.contains("#")) {
					line = line.split("#", 2)[0];
				}

				// Get the content for the entry
				splitLine = line.split("=", 2);

				// Set up the entry
				entry = makeIPSEntry();
				entry.setName(splitLine[0]);
				entry.setValue(splitLine[1]);
				entry.setId(currID);
				currID++;
				timeLoopData.addEntry(entry);
			}

			// Read in another line
			line = it.next();
		}

		// Return the parameters
		return timeLoopData;
	}

	/**
	 * Initialize a default entry for an IPS model
	 * 
	 * @return the default IPS entry
	 */
	private Entry makeIPSEntry() {
		Entry entry = new Entry() {
			protected void setup() {
				this.setName("IPS Default Entry");
				this.tag = "";
				this.ready = true;
				this.setDescription("");
				this.allowedValues = new ArrayList<String>();
				this.defaultValue = "";
				this.value = this.defaultValue;
				this.allowedValueType = AllowedValueType.Undefined;
			}
		};

		return entry;
	}
}
