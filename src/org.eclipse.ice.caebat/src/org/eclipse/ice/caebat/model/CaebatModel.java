/*******************************************************************************
 * Copyright (c) 2013, 2014 UT-Battelle, LLC.
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
package org.eclipse.ice.caebat.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ice.datastructures.ICEObject.Component;
import org.eclipse.ice.datastructures.form.Form;
import org.eclipse.ice.datastructures.form.FormStatus;
import org.eclipse.ice.io.ips.IPSReader;
import org.eclipse.ice.io.ips.IPSWriter;
import org.eclipse.ice.item.Item;

/**
 * <!-- begin-UML-doc -->
 * <p>
 * This class is the model representation of the CAEBAT model. It inherits from
 * the Item Class. It will load INI conf files into a form that can be written
 * to create new input for CAEBAT Simulations. If no conf file is given to the
 * loadInput method the CaebatModel will automatically load the case 6 example.
 * </p>
 * <!-- end-UML-doc -->
 * 
 * @author s4h, bzq
 */
@XmlRootElement(name = "CaebatModel")
public class CaebatModel extends Item {

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * A custom tag for ini files operation. Set in the constructor.
	 * </p>
	 * <!-- end-UML-doc -->
	 */
	private String customTaggedExportString = "Export to Caebat INI format";

	// The name of the example chosen
	protected String exampleName; // Default for now
	
	private ArrayList<String> actionItems;

	/**
	 * A nullary constructor that delegates to the project constructor.
	 */
	public CaebatModel() {
		this(null);
		return;
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * The constructor for the CaebatModel. Calls the constructor for Item by
	 * passing the IProject. It should call setupForm() in the super
	 * constructor.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @param project
	 *            <p>
	 *            The passed IProject for the workspace.
	 *            </p>
	 */
	public CaebatModel(IProject project) {

		// begin-user-code

		// Setup the form and everything
		super(project);
		return;
		// end-user-code

	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * This operation overrides the Item.setupForm() operation.
	 * </p>
	 * <!-- end-UML-doc -->
	 */
	public void setupForm() {
		// begin-user-code

		// This method will create a new Form and add all the dataComponents to
		// the form. These dataComponents will be accessed later in
		// loadDataComponents.
		form = new Form();

		// Setup Item information
		//setName("Caebat Model");
		//setDescription("This model creates input for CAEBAT.");

		// If loading from the new item button we should just
		// load up the default case 6 file by passing in null
		loadInput(null);

		// ----- Finish setting up the Form so that it can be immediately
		// launched

		return;
		// end-user-code

	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * This operation overrides the Item.setupItemInfo() operation.
	 * </p>
	 * <!-- end-UML-doc -->
	 */
	protected void setupItemInfo() {
		// begin-user-code

		// This method will create a new Form and add all the dataComponents to
		// the form. These dataComponents will be accessed later in
		// loadDataComponents.
	
		// Setup Item information
		setName("Caebat Model");
		setDescription("This model creates input for CAEBAT.");

		// Add an action to the list to allow for the INI exports
		customTaggedExportString = "Export to Caebat INI format";
		allowedActions.add(0, customTaggedExportString);
		actionItems = getAvailableActions();
		// end-user-code
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * Overrides the reviewEntries operation. This will still call
	 * super.reviewEntries, but will handle the dependencies after all other dep
	 * handing is finished.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @return the status of the form
	 */
	protected FormStatus reviewEntries(Form preparedForm) {

		// begin-user-code
		FormStatus retStatus = FormStatus.ReadyToProcess;
		Component dataComp = null;

		// Grab the data component from the Form and only proceed if it exists
		ArrayList<Component> components = preparedForm.getComponents();

		// Make sure the form has the right amount of data
		if (components.size() != 4) {
			System.out.println("CaebatModel Message: Could not find enough data to write a complete input format." +
					" 4 Components are required, but " + components.size() + " were found.");
			retStatus = FormStatus.InfoError;
		}
		return retStatus;
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * Overrides item's process by adding a customTaggedExportString (ini).
	 * Still utilizes Item's process functionality for all other calls.
	 * </p>
	 * <!-- end-UML-doc -->
	 */
	public FormStatus process(String actionName) {
		// begin-user-code
		FormStatus retStatus;

		System.out.println("Writing out with id " + form.getId() );
		System.out.println(actionName);
		
		// If it is the custom operation, call this here.
		if (this.customTaggedExportString.equals(actionName)) {

			// Get the file from the project space to create the output
			String filename = getName().replaceAll("\\s+", "_") + "_" + getId()
					+ ".conf";
			String filePath = project.getLocation().toOSString()
					+ System.getProperty("file.separator") + filename;

			// Get the file path and build the URI that will be used to write
			IFile outputFile = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(new Path(filePath));

			// Get the data from the form
			ArrayList<Component> components = form.getComponents();

			// A valid CaebatModel needs 4 components
			if (components.size() > 3) {

				// create a new IPSWriter with the output file
				IPSWriter writer = new IPSWriter();
				try {
					// Write the output file
					writer.write(form, outputFile);
					// Refresh the project space
					project.refreshLocal(IResource.DEPTH_ONE, null);
				} catch (CoreException e) {
					// Complain
					System.err.println("CaebatModel Message: "
							+ "Failed to refresh the project space.");
					e.printStackTrace();
				}
				// return a success
				retStatus = FormStatus.Processed;
			} else {
				// return an error
				System.err.println("Not enough components to write new file!");
				retStatus = FormStatus.InfoError;
			}
		}

		// Otherwise let item deal with the process
		else {
			retStatus = super.process(actionName);
		}

		return retStatus;
		// end-user-code

	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * This operation loads the given example into the Form.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @param name
	 *            The path name of the example file name to load.
	 */
	public void loadInput(String name) {

		// If nothing is specified, load case 6 from inside the plugin
		IFile inputFile = null;
		File temp = null;
		if (name == null) {
			try {
				// Path to the default file
				String defaultFilePath = null;
				// Create a filepath for the default file
				if (project != null) {
					defaultFilePath = project.getLocation().toOSString()
							+ System.getProperty("file.separator")
							+ "case_6.conf";
				} else {
					defaultFilePath = ResourcesPlugin.getWorkspace().getRoot()
							.getLocation().toOSString()
							+ System.getProperty("file.separator")
							+ "case_6.conf";
				}
				
				// Create a temporary location to load the default file
				temp = new File(defaultFilePath);
				if (!temp.exists()) {
					temp.createNewFile();
				}
				
				// Pull the default file from inside the plugin
				URI uri = new URI(
						"platform:/plugin/org.eclipse.ice.caebat/data/case_6.conf");
				InputStream reader = uri.toURL().openStream();
				FileOutputStream outStream = new FileOutputStream(temp);

				// Write out the default file from the plugin to the temp location
				int fileByte;
				while ((fileByte = reader.read()) != -1) {
					outStream.write(fileByte);
				}
				outStream.close();
				inputFile = ResourcesPlugin.getWorkspace().getRoot()
						.getFile(new Path(defaultFilePath));

			} catch (URISyntaxException e) {
				System.err
						.println("CaebatModel Message: Error!  Could not load the default"
								+ " Caebat case data!");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// Load a custom file
			String filePath = null;
			// Get the path to where the file will be
			if (project != null) {
				filePath = project.getLocation().toOSString()
						+ System.getProperty("file.separator") + name;
			} else {
				filePath = ResourcesPlugin.getWorkspace().getRoot()
						.getLocation().toOSString()
						+ System.getProperty("file.separator") + name;
			}
			// Get the file
			inputFile = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(new Path(filePath));
		}
		
		// Load the components from the file and setup the form
		System.out.println("CaebatModel Message: Loading" + inputFile.getFullPath().toOSString());

		IPSReader reader = new IPSReader();
		form = reader.read(inputFile);
		form.setName(getName());
		form.setDescription(getDescription());
		form.setId(getId());
		form.setItemID(getId());
		form.setActionList(actionItems);
		
		// Delete default file if it was copied into the workspace
		if (temp != null) {
			temp.delete();
		}
	}
}
