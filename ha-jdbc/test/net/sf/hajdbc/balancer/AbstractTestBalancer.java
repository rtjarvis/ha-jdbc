/*
 * HA-JDBC: High-Availability JDBC
 * Copyright (C) 2005 Paul Ferraro
 * 
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by the 
 * Free Software Foundation; either version 2.1 of the License, or (at your 
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact: ferraro@users.sourceforge.net
 */
package net.sf.hajdbc.balancer;

import java.sql.Connection;
import java.util.Arrays;
import java.util.NoSuchElementException;

import net.sf.hajdbc.Balancer;
import net.sf.hajdbc.Database;
import net.sf.hajdbc.AbstractTestCase;

/**
 * @author  Paul Ferraro
 * @since   1.0
 */
public abstract class AbstractTestBalancer extends AbstractTestCase
{
	private Balancer balancer;
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	{
		this.balancer = createBalancer();
	}
	
	protected abstract Balancer createBalancer();
	
	public void testAdd()
	{
		Database database = new MockDatabase("1", 1);
		
		boolean added = this.balancer.add(database);
		
		assertTrue(added);

		added = this.balancer.add(database);
		
		assertFalse(added);
	}

	public void testBeforeOperation()
	{
		Database database = new MockDatabase("db1", 1);

		this.balancer.add(database);
		
		this.balancer.beforeOperation(database);
	}

	public void testAfterOperation()
	{
		Database database = new MockDatabase("db1", 1);

		this.balancer.add(database);
		
		this.balancer.afterOperation(database);
	}
	
	public void testRemove()
	{
		Database database = new MockDatabase("1", 1);
		
		boolean removed = this.balancer.remove(database);

		assertFalse(removed);
		
		this.balancer.add(database);

		removed = this.balancer.remove(database);

		assertTrue(removed);
		
		removed = this.balancer.remove(database);

		assertFalse(removed);
	}

	public void testToArray()
	{
		Database[] databases = this.balancer.toArray();
		
		assertEquals(databases.length, 0);
		
		Database database1 = new MockDatabase("db1", 1);
		this.balancer.add(database1);
		
		databases = this.balancer.toArray();
		
		assertEquals(databases.length, 1);
		assertTrue(Arrays.equals(databases, new Database[] { database1 }));
		
		Database database2 = new MockDatabase("db2", 1);
		this.balancer.add(database2);

		databases = this.balancer.toArray();

		assertEquals(databases.length, 2);
		assertTrue(Arrays.equals(databases, new Database[] { database1, database2 }) || Arrays.equals(databases, new Database[] { database2, database1 }));

		this.balancer.remove(database1);

		databases = this.balancer.toArray();
		
		assertEquals(databases.length, 1);
		assertTrue(Arrays.equals(databases, new Database[] { database2, }));
		
		this.balancer.remove(database2);
		
		databases = this.balancer.toArray();
		
		assertEquals(databases.length, 0);
	}

	public void testContains()
	{
		Database database1 = new MockDatabase("db1", 1);
		Database database2 = new MockDatabase("db2", 1);

		this.balancer.add(database1);
		
		assertTrue(this.balancer.contains(database1));
		assertFalse(this.balancer.contains(database2));
	}

	public void testFirst()
	{
		try
		{
			this.balancer.first();
			
			fail();
		}
		catch (NoSuchElementException e)
		{
			// Do nothing
		}
		
		Database database = new MockDatabase("0", 0);
		
		this.balancer.add(database);
		
		Database first = this.balancer.first();
		
		assertEquals(database, first);
	}

	public void testNext()
	{
		try
		{
			this.balancer.next();
			
			fail();
		}
		catch (NoSuchElementException e)
		{
			// Do nothing
		}
		
		testNext(this.balancer);
	}
	
	protected abstract void testNext(Balancer balancer);
	
	protected class MockDatabase implements Database
	{
		private String id;
		private Integer weight;
		
		public MockDatabase(String id, int weight)
		{
			this.id = id;
			this.weight = new Integer(weight);
		}
		
		/**
		 * @see net.sf.hajdbc.Database#getId()
		 */
		public String getId()
		{
			return this.id;
		}

		/**
		 * @see net.sf.hajdbc.Database#connect(java.lang.Object)
		 */
		public Connection connect(Object connectionFactory)
		{
			return null;
		}

		/**
		 * @see net.sf.hajdbc.Database#createConnectionFactory()
		 */
		public Object createConnectionFactory()
		{
			return null;
		}

		/**
		 * @see net.sf.hajdbc.Database#getWeight()
		 */
		public Integer getWeight()
		{
			return this.weight;
		}
		
		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object object)
		{
			Database database = (Database) object;
			
			return this.id.equals(database.getId());
		}
		
		/**
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			return this.id;
		}
	}
}