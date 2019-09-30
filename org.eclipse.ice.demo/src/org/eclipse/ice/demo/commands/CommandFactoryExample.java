/*******************************************************************************
 * Copyright (c) 2019- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Examples for running Commands API package org.eclipse.ice.commands 
 *   Joe Osborn
 *******************************************************************************/
package org.eclipse.ice.demo.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.eclipse.ice.commands.Command;
import org.eclipse.ice.commands.CommandConfiguration;
import org.eclipse.ice.commands.CommandFactory;
import org.eclipse.ice.commands.CommandStatus;
import org.eclipse.ice.commands.ConnectionConfiguration;

/**
 * This class shows an example for how to use the CommandFactory class to
 * generate a Command which can execute on a local machine.
 * 
 * @author Joe Osborn
 *
 */
public class CommandFactoryExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Run an example test script
		runLocalCommand();

		// Run an example test script on a remote host
		runRemoteCommand();

		return;
	}

	/**
	 * This method runs a test dummy script on a remote host. The host ssh
	 * credentials are stored in a file in this case, for the CI build pipeline
	 * within gitlab to work. However, one could set their username/hostname in the
	 * configuration, and then enter their password when prompted.
	 */
	static void runRemoteCommand() {

		/**
		 * Create a CommandConfiguration with the necessary information to execute the
		 * command. See {@link org.eclipse.ice.commands.CommandConfiguration} for
		 * relevant member variables/constructor that one can set up.
		 */
		// Create a factory to get the Command
		CommandFactory factory = new CommandFactory();

		// Get the present working directory
		String pwd = System.getProperty("user.dir");

		// Create the path relative to the current directory where the test script lives
		String scriptDir = pwd + "/../org.eclipse.ice.commands/src/test/java/org/eclipse/ice/tests/commands/";

		String script = "./test_code_execution.sh";

		String inputFile = "someInputFile.txt";

		// Set the CommandConfiguration class
		CommandConfiguration commandConfig = new CommandConfiguration();
		commandConfig.setCommandId(1); // Give an ID to the job for tracking
		// Set the executable. Alternatively one could type setExecutable("ls -lrt")
		// for example, to list the directories in the remote host
		commandConfig.setExecutable(script);
		commandConfig.setInputFile(inputFile); // Set the input file for the script to run
		commandConfig.setErrFileName("someRemoteErrFile.txt"); // Give an error file name
		commandConfig.setOutFileName("someRemoteOutFile.txt"); // Give an out file name
		commandConfig.setNumProcs("1"); // Set the number of processes
		commandConfig.setInstallDirectory(""); // Set the install directory where libraries live, if needed
		commandConfig.setWorkingDirectory(scriptDir); // Set the working directory where the scripts live
		commandConfig.setAppendInput(true); // Append the input file to the script
		commandConfig.setOS(System.getProperty("os.name")); // Get the OS
		// Set the remote working directory for the process to be performed
		commandConfig.setRemoteWorkingDirectory("/tmp/remoteCommandTestDirectory");

		System.out.println(scriptDir);
		/**
		 * Create a ConnectionConfiguration with the necessary information to open a
		 * remote connection. See
		 * {@link org.eclipse.ice.commands.ConnectionConfiguration} for relevant member
		 * variables that one can set. Note about passwords: The password can be set to
		 * open the connection; however, it is in general not recommended since Strings
		 * are immutable and thus the password could in principle be identified with a
		 * code profiler. The password is entered here for the dummy ssh account set up
		 * for the CI build pipeline. Users have two options:
		 * 
		 * 1. They can set their password as shown below
		 * 
		 * 2. They can not set a password and they will be prompted for the password at
		 * the console once the connection is trying to be established. This password is
		 * received in an array of chars and subsequently deleted once the connection is
		 * established.
		 */
		ConnectionConfiguration connectionConfig = new ConnectionConfiguration();
		// Set the connection configuration to a dummy remote connection
		// Read in a dummy configuration file that contains credentials
		File file = new File("/tmp/ice-remote-creds.txt");
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		// Scan line by line
		scanner.useDelimiter("\n");

		// Get the credentials for the dummy remote account
		String username = scanner.next();
		String password = scanner.next();
		String hostname = scanner.next();

		// Make the connection configuration
		connectionConfig.setHostname(hostname);
		connectionConfig.setUsername(username);
		connectionConfig.setPassword(password);
		// Give the connection a name
		connectionConfig.setName("dummyConnection");

		// Delete the remote working directory once we are finished running the job
		connectionConfig.setDeleteWorkingDirectory(true);

		// Get the command
		Command command = null;
		try {
			command = factory.getCommand(commandConfig, connectionConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Run the command
		CommandStatus status = command.execute();

		// Ensure it finished properly
		assert (status == CommandStatus.SUCCESS);
	}

	/**
	 * This method runs a test dummy script on one's local computer. The dummy
	 * script is locating in the JUnit test directory of the Commands API project.
	 */
	static void runLocalCommand() {

		/**
		 * Create a CommandConfiguration with the necessary information to execute a
		 * Command. See {@link org.eclipse.ice.commands.CommandConfiguration} for
		 * relevant member variables/constructor.
		 */

		// Create a factory to get the Command
		CommandFactory factory = new CommandFactory();

		// Get the local hostname for processing
		String hostname = getLocalHostname();

		// Get the present working directory
		String pwd = System.getProperty("user.dir");

		// Create the path relative to the current directory where the test script lives
		String scriptDir = pwd + "/../org.eclipse.ice.commands/src/test/java/org/eclipse/ice/tests/commands/";

		// Create the script path
		String script = "./test_code_execution.sh";

		// Create the input file path
		String inputFile = "someInputFile.txt";

		// Set the CommandConfiguration class
		CommandConfiguration commandConfig = new CommandConfiguration();
		commandConfig.setCommandId(1);
		commandConfig.setExecutable(script);
		commandConfig.setInputFile(inputFile);
		commandConfig.setErrFileName("someLocalErrFile.txt");
		commandConfig.setOutFileName("someLocalOutFile.txt");
		commandConfig.setNumProcs("1");
		commandConfig.setInstallDirectory("");
		commandConfig.setWorkingDirectory(scriptDir);
		commandConfig.setAppendInput(true);
		commandConfig.setOS(System.getProperty("os.name"));

		// Make a ConnectionConfiguration to indicate that we want to run locally
		ConnectionConfiguration connectionConfig = new ConnectionConfiguration();
		connectionConfig.setHostname(hostname);

		// Get the command
		Command localCommand = null;
		try {
			localCommand = factory.getCommand(commandConfig, connectionConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Run it
		CommandStatus status = localCommand.execute();

		System.out.println("Status of Command after execution: " + status);

		// Get a string of the output that is produced from the job
		String output = commandConfig.getStdOutputString();

		return;
	}

	/**
	 * This function just returns the local hostname of your local computer
	 * 
	 * @return - String - local hostname
	 */
	protected static String getLocalHostname() {
		// Get the hostname for your local computer
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		String hostname = addr.getHostName();

		return hostname;
	}

}
