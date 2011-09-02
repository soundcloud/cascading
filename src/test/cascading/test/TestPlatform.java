/*
 * Copyright (c) 2007-2011 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Cascading is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cascading is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cascading.  If not, see <http://www.gnu.org/licenses/>.
 */

package cascading.test;

import java.io.IOException;
import java.util.Map;

import cascading.flow.FlowConnector;
import cascading.flow.FlowProcess;
import cascading.tap.SinkMode;
import cascading.tap.Tap;
import cascading.tuple.Fields;

/**
 *
 */
public abstract class TestPlatform
  {
  public static final String CLUSTER_TESTING_PROPERTY = "test.cluster.enabled";
  public static final String TEST_PLATFORM_CLASSNAME = "test.platform.classname";

  private boolean useCluster = false;
  private boolean enableCluster = true;

  protected TestPlatform()
    {
    enableCluster = Boolean.parseBoolean( System.getProperty( CLUSTER_TESTING_PROPERTY, Boolean.toString( enableCluster ) ) );
    }

  public abstract void setUp() throws IOException;

  public abstract Map<Object, Object> getProperties();

  public abstract void tearDown();

  public void setUseCluster( boolean useCluster )
    {
    this.useCluster = useCluster;
    }

  public boolean isUseCluster()
    {
    return enableCluster && useCluster;
    }

  public abstract void copyFromLocal( String inputFile ) throws IOException;

  public abstract void copyToLocal( String outputFile ) throws IOException;

  public abstract FlowProcess getFlowProcess();

  public abstract FlowConnector getFlowConnector( Map<Object, Object> properties );

  public FlowConnector getFlowConnector()
    {
    return getFlowConnector( getProperties() );
    }

  public Tap getTextFile( Fields sourceFields, String filename )
    {
    return getTextFile( sourceFields, filename, SinkMode.KEEP );
    }

  public Tap getTextFile( String filename )
    {
    return getTextFile( filename, SinkMode.KEEP );
    }

  public Tap getTextFile( String filename, SinkMode mode )
    {
    return getTextFile( null, filename, mode );
    }

  public Tap getTextFile( Fields sourceFields, String filename, SinkMode mode )
    {
    return getTextFile( sourceFields, Fields.ALL, filename, mode );
    }

  public abstract Tap getTextFile( Fields sourceFields, Fields sinkFields, String filename, SinkMode mode );

  public Tap getBinaryFile( Fields fields, String filename )
    {
    return getBinaryFile( fields, filename );
    }

  public Tap getDelimitedFile( Fields fields, String delimiter, String filename )
    {
    return getDelimitedFile( fields, false, delimiter, "\"", null, filename, SinkMode.KEEP );
    }

  public Tap getDelimitedFile( Fields fields, String delimiter, String filename, SinkMode mode )
    {
    return getDelimitedFile( fields, false, delimiter, "\"", null, filename, mode );
    }

  public Tap getDelimitedFile( Fields fields, String filename, SinkMode mode )
    {
    return getDelimitedFile( fields, false, "\t", "\"", null, filename, mode );
    }

  public Tap getDelimitedFile( Fields fields, String delimiter, Class[] types, String filename, SinkMode mode )
    {
    return getDelimitedFile( fields, false, delimiter, "", types, filename, mode );
    }

  public abstract Tap getDelimitedFile( Fields fields, boolean skipHeader, String delimiter, String quote, Class[] types, String filename, SinkMode mode );
  }