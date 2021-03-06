/*******************************************************************************
 * Copyright (c) 2011, 2014 UT-Battelle, LLC.
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
package org.eclipse.ice.tests.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.eclipse.ice.datastructures.ICEObject.ICEJAXBHandler;
import org.eclipse.ice.datastructures.ICEObject.ICEObject;
import org.junit.Test;

/**
 * The ICEObjectTester is responsible for testing the ICEObject class. It only
 * tests the name, id, and description properties as well as persistence. It
 * also checks equality, hashCode computation, copying, and cloning.
 * 
 * @author Jay Jay Billings
 */
public class ICEObjectTester {

	/**
	 * This operation checks the ICEObject to insure that the id, name and
	 * description getters and setters function properly.
	 */
	@Test
	public void checkProperties() {

		// Local declarations
		int id = 20110901;
		String name = "September 1st 2011";
		String description = "The 1st day of the ninth month in the year of "
				+ "our Lord 2011";

		// Create the ICEObject
		ICEObject testObject = new ICEObject();

		// Set up the id, name and description
		testObject.setId(id);
		testObject.setName(name);
		testObject.setDescription(description);

		// Check the id, name and description
		assertEquals(testObject.getId(), id);
		assertEquals(testObject.getName(), name);
		assertEquals(testObject.getDescription(), description);

		// Check the context
		assertEquals("ice-default", testObject.getContext());
		testObject.setContext("foo");
		assertEquals("foo", testObject.getContext());

		return;
	}

	/**
	 * This operation checks the ICEObject class to ensure that its copy() and
	 * clone() operations work as specified.
	 */
	@Test
	public void checkCopying() {

		// Local declarations
		int id = 20110901;
		String name = "September 1st 2011";
		String description = "The 1st day of the ninth month in the year of "
				+ "our Lord 2011";
		ICEObject testNC = new ICEObject();

		// Test to show valid usage of clone

		// Set up the id, name and description
		testNC.setId(id);
		testNC.setName(name);
		testNC.setDescription(description);
		testNC.setContext("foo");

		// Run clone operation
		ICEObject cloneNC = (ICEObject) testNC.clone();

		// Check the id, name and description with clone
		assertEquals(testNC.getId(), cloneNC.getId());
		assertEquals(testNC.getName(), cloneNC.getName());
		assertEquals(testNC.getDescription(), cloneNC.getDescription());

		// Test to show valid usage of copy

		// Local declarations
		id = 20110901;
		name = "September 1st 2011";
		description = "The 1st day of the ninth month in the year of "
				+ "our Lord 2011";
		testNC = new ICEObject();

		// Set up the id, name and description
		testNC.setId(id);
		testNC.setName(name);
		testNC.setDescription(description);

		// Create a new instance of ICEObject and copy contents
		ICEObject testNC2 = new ICEObject();
		testNC2.copy(testNC);

		// Check the id, name and description with copy
		assertEquals(testNC.getId(), testNC2.getId());
		assertEquals(testNC.getName(), testNC2.getName());
		assertEquals(testNC.getDescription(), testNC2.getDescription());
		assertEquals(testNC.getContext(), testNC2.getContext());

		// Test to show an invalid use of copy - null args

		// Local declarations
		id = 20110901;
		name = "September 1st 2011";
		description = "The 1st day of the ninth month in the year of "
				+ "our Lord 2011";
		testNC = new ICEObject();

		// Set up the id, name and description
		testNC.setId(id);
		testNC.setName(name);
		testNC.setDescription(description);
		// Attempt the null copy
		testNC.copy(null);

		// Check the id, name and description - nothing has changed
		assertEquals(testNC.getId(), id);
		assertEquals(testNC.getName(), name);
		assertEquals(testNC.getDescription(), description);

	}

	/**
	 * <p>
	 * This operation checks the ability of the ICEObject to persist itself to
	 * XML and to load itself from an XML input stream.
	 * </p>
	 * 
	 * @throws IOException
	 * @throws JAXBException
	 * @throws NullPointerException
	 * 
	 */
	@Test
	public void checkXMLPersistence()
			throws NullPointerException, JAXBException, IOException {
		// TODO Auto-generated method stub

		/*
		 * The following sets of operations will be used to test the
		 * "read and write" portion of the ICEObject. It will demonstrate the
		 * behavior of reading and writing from an
		 * "XML (inputStream and outputStream)" file. It will use an annotated
		 * ICEObject to demonstrate basic behavior.
		 */

		// Local declarations
		ICEObject testNC = null, testNC2 = null;
		int id = 20110901;
		String name = "September 1st 2011";
		String description = "The 1st day of the ninth month in the year of "
				+ "our Lord 2011";
		ICEJAXBHandler xmlHandler = new ICEJAXBHandler();
		ArrayList<Class> classList = new ArrayList<Class>();
		classList.add(ICEObject.class);

		// Demonstrate a basic "write" to file. Should not fail
		// Initialize the object and set values.
		testNC = new ICEObject();
		testNC.setId(id);
		testNC.setName(name);
		testNC.setDescription(description);

		// persist to an output stream
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		xmlHandler.write(testNC, classList, outputStream);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				outputStream.toByteArray());

		// Convert to inputStream
		testNC2 = (ICEObject) xmlHandler.read(classList, inputStream);

		// Check that it equals the persisted object
		assertTrue(testNC.equals(testNC2));

	}

	/**
	 * <p>
	 * This operation checks the ICEObject class to insure that its equals()
	 * operation works.
	 * </p>
	 * 
	 */
	@Test
	public void checkEquality() {

		// Create an ICEObject
		ICEObject testICEObject = new ICEObject();

		// Set its data
		testICEObject.setId(12);
		testICEObject.setName("ICE ICEObject");
		testICEObject.setDescription("This is an ICEObject that will "
				+ "be used for testing equality with other ICEObjects.");
		testICEObject.setContext("foo");

		// Create another ICEObject to assert Equality with the last
		ICEObject equalObject = new ICEObject();

		// Set its data, equal to testICEObject
		equalObject.setId(12);
		equalObject.setName("ICE ICEObject");
		equalObject.setDescription("This is an ICEObject that will "
				+ "be used for testing equality with other ICEObjects.");
		equalObject.setContext("foo");

		// Create an ICEObject that is not equal to testICEObject
		ICEObject unEqualObject = new ICEObject();

		// Set its data, not equal to testICEObject
		unEqualObject.setId(52);
		unEqualObject.setName("Bill the ICEObject");
		unEqualObject.setDescription("This is an ICEObject to verify that "
				+ "ICEObject.equals() returns false for an object that is not "
				+ "equivalent to testICEObject.");

		// Create a third ICEObject to test Transitivity
		ICEObject transitiveObject = new ICEObject();

		// Set its data, not equal to testICEObject
		transitiveObject.setId(12);
		transitiveObject.setName("ICE ICEObject");
		transitiveObject.setDescription("This is an ICEObject that will "
				+ "be used for testing equality with other ICEObjects.");
		transitiveObject.setContext("foo");

		// Assert that these two ICEObjects are equal
		assertTrue(testICEObject.equals(equalObject));

		// Assert that two unequal objects returns false
		assertFalse(testICEObject.equals(unEqualObject));

		// Check that equals() is Reflexive
		// x.equals(x) = true
		assertTrue(testICEObject.equals(testICEObject));

		// Check that equals() is Symmetric
		// x.equals(y) = true iff y.equals(x) = true
		assertTrue(testICEObject.equals(equalObject)
				&& equalObject.equals(testICEObject));

		// Check that equals() is Transitive
		// x.equals(y) = true, y.equals(z) = true => x.equals(z) = true
		if (testICEObject.equals(equalObject)
				&& equalObject.equals(transitiveObject)) {
			assertTrue(testICEObject.equals(transitiveObject));
		} else {
			fail();
		}

		// Check the Consistent nature of equals()
		assertTrue(testICEObject.equals(equalObject)
				&& testICEObject.equals(equalObject)
				&& testICEObject.equals(equalObject));
		assertTrue(!testICEObject.equals(unEqualObject)
				&& !testICEObject.equals(unEqualObject)
				&& !testICEObject.equals(unEqualObject));

		// Assert checking equality with null value returns false
		assertFalse(testICEObject == null);

		// Assert that two equal objects have the same hashcode
		assertTrue(testICEObject.equals(equalObject)
				&& testICEObject.hashCode() == equalObject.hashCode());

		// Assert that hashcode is consistent
		assertTrue(testICEObject.hashCode() == testICEObject.hashCode());

		// Assert that hashcodes are different for unequal objects
		assertFalse(testICEObject.hashCode() == unEqualObject.hashCode());

	}

	/**
	 * <p>
	 * This operation tests the ICEObject to insure that it can properly
	 * dispatch notifications when it receives an update that changes its state.
	 * </p>
	 * 
	 */
	@Test
	public void checkNotifications() {

		// Setup the listeners
		TestComponentListener firstListener = new TestComponentListener();
		TestComponentListener secondListener = new TestComponentListener();

		// Setup the iceObject
		ICEObject iceObject = new ICEObject();

		// Register the listener
		iceObject.register(firstListener);

		// Add the second listener
		iceObject.register(secondListener);

		// Change the name of the object
		iceObject.setName("Warren Buffett");
		// Check the listeners to make sure they updated
		assertTrue(firstListener.wasNotified());
		assertTrue(secondListener.wasNotified());
		// Reset the listeners
		firstListener.reset();
		secondListener.reset();

		// Unregister the second listener so that it no longer receives updates
		iceObject.unregister(secondListener);

		// Change the id of the object
		iceObject.setId(899);
		assertTrue(firstListener.wasNotified());
		// Make sure the second listener was not updated
		assertFalse(secondListener.wasNotified());

		// Reset the listener
		firstListener.reset();
		// Change the description of the object
		iceObject.setDescription("New description");
		// Make sure the listener was notified
		assertTrue(firstListener.wasNotified());

		return;
	}
}