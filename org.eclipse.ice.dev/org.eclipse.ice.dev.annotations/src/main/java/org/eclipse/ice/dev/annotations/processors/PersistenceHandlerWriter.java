/*******************************************************************************
 * Copyright (c) 2020- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Bluhm - Initial implementation
 *******************************************************************************/

package org.eclipse.ice.dev.annotations.processors;

import lombok.Builder;
import lombok.NonNull;

/**
 * Writer for DataElement Persistence classes.
 * @author Daniel Bluhm
 */
public class PersistenceHandlerWriter extends VelocitySourceWriter {

	/**
	 * Location of PersistenceHandler template for use with velocity.
	 *
	 * Use of Velocity ClasspathResourceLoader means files are discovered relative
	 * to the src/main/resources folder.
	 */
	private static final String PERSISTENCE_HANDLER_TEMPLATE = "templates/PersistenceHandler.vm";

	/**
	 * Context key for package.
	 */
	private static final String PACKAGE = "package";

	/**
	 * Context key for element interface.
	 */
	private static final String ELEMENT_INTERFACE = "elementInterface";

	/**
	 * Context key for class
	 */
	private static final String CLASS = "class";

	/**
	 * Context key for collection.
	 */
	private static final String COLLECTION = "collection";

	/**
	 * Context key for implementation.
	 */
	private static final String IMPLEMENTATION = "implementation";

	/**
	 * Context key for fields.
	 */
	private static final String FIELDS = "fields";

	@Builder
	public PersistenceHandlerWriter(
		String packageName, String elementInterface, String className,
		String implementation, String collection, @NonNull Fields fields
	) {
		super();
		this.template = PERSISTENCE_HANDLER_TEMPLATE;
		this.context.put(PACKAGE, packageName);
		this.context.put(ELEMENT_INTERFACE, elementInterface);
		this.context.put(CLASS, className);
		this.context.put(COLLECTION, collection);
		this.context.put(IMPLEMENTATION, implementation);
		this.context.put(FIELDS, fields);
	}
}