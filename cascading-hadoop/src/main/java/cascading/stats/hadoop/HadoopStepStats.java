/*
 * Copyright (c) 2007-2015 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cascading.stats.hadoop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cascading.CascadingException;
import cascading.flow.FlowStep;
import cascading.management.state.ClientState;
import cascading.util.Util;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TaskCompletionEvent;
import org.apache.hadoop.mapred.TaskID;
import org.apache.hadoop.mapred.TaskReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hadoop 1 specific sub-class of BaseHadoopStats using the mapred API.
 */
public abstract class HadoopStepStats extends BaseHadoopStepStats
  {
  /** logger. */
  private static final Logger LOG = LoggerFactory.getLogger( BaseHadoopStepStats.class );

  private Map<TaskID, String> idCache = new HashMap<TaskID, String>( 4999 ); // nearest prime, caching for ids

  protected HadoopStepStats( FlowStep<JobConf> flowStep, ClientState clientState )
    {
    super( flowStep, clientState );
    }

  @Override
  protected void addTaskStats( Map<String, HadoopSliceStats> taskStats, HadoopSliceStats.Kind kind, boolean skipLast ) throws IOException
    {
    TaskReport[] taskReports = retrieveTaskReports( kind );

    for( int i = 0; i < taskReports.length - ( skipLast ? 1 : 0 ); i++ )
      {
      TaskReport taskReport = taskReports[ i ];

      if( taskReport == null )
        {
        LOG.warn( "found empty task report" );
        continue;
        }

      String id = getIDFor( taskReport.getTaskID() );
      taskStats.put( id, new HadoopSliceStats( id, getStatus(), kind, stepHasReducers(), taskReport ) );

      incrementKind( kind );
      }
    }

  /**
   * Retrieves the TaskReports via the mapred API.
   */
  private TaskReport[] retrieveTaskReports( HadoopSliceStats.Kind kind ) throws IOException
    {
    JobClient jobClient = getJobClient();
    RunningJob runningJob = getRunningJob();

    if( jobClient == null || runningJob == null )
      return new TaskReport[ 0 ];

    switch( kind )
      {
      case MAPPER:
        return jobClient.getMapTaskReports( runningJob.getID() );
      case REDUCER:
        return jobClient.getReduceTaskReports( runningJob.getID() );
      case CLEANUP:
        return jobClient.getCleanupTaskReports( runningJob.getID() );
      case SETUP:
        return jobClient.getSetupTaskReports( runningJob.getID() );
      default:
        return new TaskReport[ 0 ];
      }
    }

  @Override
  protected void addAttemptsToTaskStats( Map<String, HadoopSliceStats> taskStats, boolean captureAttempts )
    {
    RunningJob runningJob = getRunningJob();

    if( runningJob == null )
      return;

    int count = 0;

    while( captureAttempts )
      {
      try
        {
        TaskCompletionEvent[] events = runningJob.getTaskCompletionEvents( count );

        if( events.length == 0 )
          break;

        for( TaskCompletionEvent event : events )
          {
          if( event == null )
            {
            LOG.warn( "found empty completion event" );
            continue;
            }

          // this will return a housekeeping task, which we are not tracking
          HadoopSliceStats stats = taskStats.get( getIDFor( event.getTaskAttemptId().getTaskID() ) );

          if( stats != null )
            stats.addAttempt( event );
          }

        count += events.length;
        }
      catch( IOException exception )
        {
        throw new CascadingException( exception );
        }
      }
    }

  private String getIDFor( TaskID taskID )
    {
    // using taskID instance as #toString is quite painful
    String id = idCache.get( taskID );

    if( id == null )
      {
      id = Util.createUniqueID();
      idCache.put( taskID, id );
      }

    return id;
    }
  }
