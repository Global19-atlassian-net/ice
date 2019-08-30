/*******************************************************************************
 * Copyright (c) 2019- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - 
 *   Jay Jay Billings, Joe Osborn
 *******************************************************************************/
package org.eclipse.ice.tests.commands;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.ice.commands.Command;
import org.eclipse.ice.commands.CommandConfiguration;
import org.eclipse.ice.commands.CommandFactory;
import org.eclipse.ice.commands.CommandStatus;
import org.eclipse.ice.commands.ConnectionConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests {@link org.eclipse.ice.commands.CommandFactory}.
 * 
 * @author Jay Jay Billings, Joe Osborn
 *
 */
public class CommandFactoryTest {

	/**
	 * The hostname for which the job should run on. Default to local host name for
	 * now
	 */
	String hostname = getLocalHostname();

	/**
	 * Test method for {@link org.eclipse.ice.commands.CommandFactory#getCommand()}
	 * and for the whole {@link org.eclipse.ice.commands.LocalCommand#execute()}
	 * execution chain with a fully functional command dictionary
	 */

	public CommandFactoryTest() {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testFunctionalLocalCommand() {

		/**
		 * Create a CommandConfiguration with the necessary information to execute a
		 * Command. See {@link org.eclipse.ice.commands.CommandConfiguration} for
		 * relevant member variables/constructor.
		 */
		/**
		 * This is a test with real files to test an actual job processing. For this
		 * test to work, make sure you change the workingDirectory to your actual
		 * workingDirectory where the Commands API lives
		 */

		// Set the CommandConfiguration class
		CommandConfiguration commandConfig = new CommandConfiguration(1, "./test_code_execution.sh", "someInputFile.txt",
				"someOutFile.txt", "someErrFile.txt", "1", "osx", "",
				"/Users/4jo/git/icefork2/org.eclipse.ice.commands/src/test/java/org/eclipse/ice/tests/commands", true);

		ConnectionConfiguration connectionConfig = new ConnectionConfiguration("username","password",hostname);
		
		// Get the command
		Command localCommand = null;
		try {
			localCommand = CommandFactory.getCommand(commandConfig, connectionConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Run it
		CommandStatus status = localCommand.execute();

		assert (status == CommandStatus.SUCCESS);

	}

	/**
	 * Test method for {@link org.eclipse.ice.commands.CommandFactory#getCommand()}
	 * and for the whole {@link org.eclipse.ice.commands.LocalCommand#execute()}
	 * execution chain with an uncompleted Command dictionary. This is function is
	 * intended to test some of the exception catching, thus it is expected to
	 * "fail." It expect a null pointer exception, since the hostname is not given
	 * and thus the hostname string is null
	 */
	@Test(expected = NullPointerException.class)
	public void testNonFunctionalLocalCommand() {

		System.out.println("\nTesting some commands where not enough command information was provided.");

		//Create a command configuration that doesn't have all the necessary information
		// Set the CommandConfiguration class
		CommandConfiguration commandConfig = new CommandConfiguration();
		
		
		ConnectionConfiguration connectConfig = new ConnectionConfiguration("uname","pwd",hostname);
		// Get the command
		Command localCommand = null;
		try {
			localCommand = CommandFactory.getCommand(commandConfig, connectConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Run it and expect that it fails
		CommandStatus status = localCommand.execute();

		assert (status == CommandStatus.INFOERROR);

	}

	/**
	 * Test method for {@link org.eclipse.ice.commands.CommandFactory#getCommand()}
	 * and for the whole {@link org.eclipse.ice.commands.LocalCommand#execute()}
	 * execution chain with an uncompleted Command dictionary. This function is
	 * intended to test some of the exception catching, thus it is expected to
	 * "fail."
	 */
	@Test
	public void testIncorrectWorkingDirectory() {
		/**
		 * Run another non functional command, with a non existing working directory
		 */

		System.out.println("\nTesting some commands where not enough command information was provided.");

		// Set the CommandConfiguration class
		CommandConfiguration commandConfiguration = new CommandConfiguration(1, 
				"./test_code_execution.sh", "someInputFile.txt", "someOutFile.txt",
				"someErrFile.txt", "1", "~/installDir", "osx", "~/some_nonexistant_directory", true);

		ConnectionConfiguration connectConfig = new ConnectionConfiguration("uname","pwd",hostname);
		// Get the command
		Command localCommand2 = null;
		try {
			localCommand2 = CommandFactory.getCommand(commandConfiguration, connectConfig);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Run it and expect that it fails
		CommandStatus status2 = localCommand2.execute();

		assert (status2 == CommandStatus.FAILED);
	}

	/**
	 * This function just returns the local hostname of your local computer. It is
	 * useful for testing a variety of local commands.
	 * 
	 * @return - String - local hostname
	 */
	protected String getLocalHostname() {
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
