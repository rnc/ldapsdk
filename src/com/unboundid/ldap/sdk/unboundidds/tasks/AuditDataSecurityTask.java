/*
 * Copyright 2011-2016 UnboundID Corp.
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2015-2016 UnboundID Corp.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPLv2 only)
 * or the terms of the GNU Lesser General Public License (LGPLv2.1 only)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.unboundid.ldap.sdk.unboundidds.tasks;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.util.NotMutable;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;

import static com.unboundid.ldap.sdk.unboundidds.tasks.TaskMessages.*;
import static com.unboundid.util.Validator.*;



/**
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class is part of the Commercial Edition of the UnboundID
 *   LDAP SDK for Java.  It is not available for use in applications that
 *   include only the Standard Edition of the LDAP SDK, and is not supported for
 *   use in conjunction with non-UnboundID products.
 * </BLOCKQUOTE>
 * This class defines a Directory Server task that can be used to cause the
 * server to initiate a data security audit, which can look for potential
 * issues in the environment that can impact the security of the directory
 * environment.
 * <BR><BR>
 * The properties that are available for use with this type of task include:
 * <UL>
 *   <LI>The names of the auditors to include or exclude from the audit.  This
 *       is optional, and if it is not provided, then all enabled auditors will
 *       be used.</LI>
 *   <LI>The backend IDs for the backends containing the data to be audited.
 *       This is optional, and if it is not provided then the server will run
 *       the audit in all backends that support this capability.</LI>
 *   <LI>A set of filters which identify the entries that should be examined by
 *       the audit.  This is optional, and if it is not provided, then all
 *       entries in the selected backends will be included.</LI>
 *   <LI>The path to the directory in which the output files should be
 *       generated.  This is optional, and if it is not provided then the server
 *       will use a default output directory.</LI>
 * </UL>
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class AuditDataSecurityTask
       extends Task
{
  /**
   * The fully-qualified name of the Java class that is used for the audit data
   * security task.
   */
  static final String AUDIT_DATA_SECURITY_TASK_CLASS =
       "com.unboundid.directory.server.tasks.AuditDataSecurityTask";



  /**
   * The name of the attribute used to specify the set of auditors to use to
   * examine the data.
   */
  private static final String ATTR_INCLUDE_AUDITOR =
       "ds-task-audit-data-security-include-auditor";



  /**
   * The name of the attribute used to specify the set of auditors that should
   * not be used when examining the data.
   */
  private static final String ATTR_EXCLUDE_AUDITOR =
       "ds-task-audit-data-security-exclude-auditor";



  /**
   * The name of the attribute used to the backend IDs for the backends in which
   * the audit should be performed.
   */
  private static final String ATTR_BACKEND_ID =
       "ds-task-audit-data-security-backend-id";



  /**
   * The name of the attribute used to specify a set of filters that should be
   * used to identify entries to include in the audit.
   */
  private static final String ATTR_REPORT_FILTER =
       "ds-task-audit-data-security-report-filter";



  /**
   * The name of the attribute used to specify the directory in which the report
   * output files should be written.
   */
  private static final String ATTR_OUTPUT_DIRECTORY =
       "ds-task-audit-data-security-output-directory";



  /**
   * The name of the object class used in audit data security task entries.
   */
  private static final String OC_AUDIT_DATA_SECURITY_TASK =
       "ds-task-audit-data-security";



  /**
   * The task property that will be used for the included set of auditors.
   */
  private static final TaskProperty PROPERTY_INCLUDE_AUDITOR =
       new TaskProperty(ATTR_INCLUDE_AUDITOR,
            INFO_AUDIT_DATA_SECURITY_DISPLAY_NAME_INCLUDE_AUDITOR.get(),
            INFO_AUDIT_DATA_SECURITY_DESCRIPTION_INCLUDE_AUDITOR.get(),
            String.class, false, true, false);



  /**
   * The task property that will be used for the excluded set of auditors.
   */
  private static final TaskProperty PROPERTY_EXCLUDE_AUDITOR =
       new TaskProperty(ATTR_EXCLUDE_AUDITOR,
            INFO_AUDIT_DATA_SECURITY_DISPLAY_NAME_EXCLUDE_AUDITOR.get(),
            INFO_AUDIT_DATA_SECURITY_DESCRIPTION_EXCLUDE_AUDITOR.get(),
            String.class, false, true, false);



  /**
   * The task property that will be used for the backend IDs.
   */
  private static final TaskProperty PROPERTY_BACKEND_ID =
       new TaskProperty(ATTR_BACKEND_ID,
            INFO_AUDIT_DATA_SECURITY_DISPLAY_NAME_BACKEND_ID.get(),
            INFO_AUDIT_DATA_SECURITY_DESCRIPTION_BACKEND_ID.get(),
            String.class, false, true, false);



  /**
   * The task property that will be used for the report filters.
   */
  private static final TaskProperty PROPERTY_REPORT_FILTER =
       new TaskProperty(ATTR_REPORT_FILTER,
            INFO_AUDIT_DATA_SECURITY_DISPLAY_NAME_REPORT_FILTER.get(),
            INFO_AUDIT_DATA_SECURITY_DESCRIPTION_REPORT_FILTER.get(),
            String.class, false, true, false);



  /**
   * The task property that will be used for the output directory.
   */
  private static final TaskProperty PROPERTY_OUTPUT_DIRECTORY =
       new TaskProperty(ATTR_OUTPUT_DIRECTORY,
            INFO_AUDIT_DATA_SECURITY_DISPLAY_NAME_OUTPUT_DIR.get(),
            INFO_AUDIT_DATA_SECURITY_DESCRIPTION_OUTPUT_DIR.get(),
            String.class, false, false, false);



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = -4994621474763299632L;



  // The backend IDs of the backends in which the audit should be performed.
  private final List<String> backendIDs;

  // The names of the excluded auditors to use in the audit.
  private final List<String> excludeAuditors;

  // The names of the included auditors to use in the audit.
  private final List<String> includeAuditors;

  // The report filters to select entries to audit.
  private final List<String> reportFilters;

  // The path of the output directory to use for report data files.
  private final String outputDirectory;



  /**
   * Creates a new uninitialized audit data security task instance which should
   * only be used for obtaining general information about this task, including
   * the task name, description, and supported properties.  Attempts to use a
   * task created with this constructor for any other reason will likely fail.
   */
  public AuditDataSecurityTask()
  {
    excludeAuditors = null;
    includeAuditors = null;
    backendIDs      = null;
    reportFilters   = null;
    outputDirectory = null;
  }



  /**
   * Creates a new audit data security task with the provided information and
   * default settings for all general task properties.
   *
   * @param  includeAuditors  The names of the auditors that should be used to
   *                          examine the data.  It may be {@code null} or empty
   *                          if an exclude list should be provided, or if all
   *                          enabled auditors should be invoked.  You must not
   *                          provide both include and exclude auditors.
   * @param  excludeAuditors  The names of the auditors that should be excluded
   *                          when examining the data.  It may be {@code null}
   *                          or empty if an include list should be provided, or
   *                          if all enabled auditors should be invoked.  You
   *                          must not provide both include and exclude
   *                          auditors.
   * @param  backendIDs       The backend IDs of the backends containing the
   *                          data to examine.  It may be {@code null} or empty
   *                          if all supported backends should be selected.
   * @param  reportFilters    A set of filters which identify entries that
   *                          should be examined.  It may be {@code null} or
   *                          empty if all entries should be examined.
   * @param  outputDirectory  The path to the output directory (on the server
   *                          filesystem) in which report data files should be
   *                          written.  It may be {@code null} if a default
   *                          output directory should be used.
   */
  public AuditDataSecurityTask(final List<String> includeAuditors,
                               final List<String> excludeAuditors,
                               final List<String> backendIDs,
                               final List<String> reportFilters,
                               final String outputDirectory)
  {
    this(null, includeAuditors, excludeAuditors, backendIDs, reportFilters,
         outputDirectory, null, null, null, null, null);
  }



  /**
   * Creates a new audit data security task with the provided information.
   *
   * @param  taskID                  The task ID to use for this task.  If it is
   *                                 {@code null} then a UUID will be generated
   *                                 for use as the task ID.
   * @param  includeAuditors         The names of the auditors that should be
   *                                 used to examine the data.  It may be
   *                                 {@code null} or empty if an exclude list
   *                                 should be provided, or if all enabled
   *                                 auditors should be invoked.  You must not
   *                                 provide both include and exclude auditors.
   * @param  excludeAuditors         The names of the auditors that should be
   *                                 excluded when examining the data.  It may
   *                                 be {@code null} or empty if an include list
   *                                 should be provided, or if all enabled
   *                                 auditors should be invoked.  You must not
   *                                 provide both include and exclude auditors.
   * @param  backendIDs              The backend IDs of the backends containing
   *                                 the data to examine.  It may be
   *                                 {@code null} or empty if all supported
   *                                 backends should be selected.
   * @param  reportFilters           A set of filters which identify entries
   *                                 that should be examined.  It may be
   *                                 {@code null} or empty if all entries should
   *                                 be examined.
   * @param  outputDirectory         The path to the output directory (on the
   *                                 server filesystem) in which report data
   *                                 files should be written.  It may be
   *                                 {@code null} if a default output directory
   *                                 should be used.
   * @param  scheduledStartTime      The time that this task should start
   *                                 running.
   * @param  dependencyIDs           The list of task IDs that will be required
   *                                 to complete before this task will be
   *                                 eligible to start.
   * @param  failedDependencyAction  Indicates what action should be taken if
   *                                 any of the dependencies for this task do
   *                                 not complete successfully.
   * @param  notifyOnCompletion      The list of e-mail addresses of individuals
   *                                 that should be notified when this task
   *                                 completes.
   * @param  notifyOnError           The list of e-mail addresses of individuals
   *                                 that should be notified if this task does
   *                                 not complete successfully.
   */
  public AuditDataSecurityTask(final String taskID,
              final List<String> includeAuditors,
              final List<String> excludeAuditors, final List<String> backendIDs,
              final List<String> reportFilters, final String outputDirectory,
              final Date scheduledStartTime, final List<String> dependencyIDs,
              final FailedDependencyAction failedDependencyAction,
              final List<String> notifyOnCompletion,
              final List<String> notifyOnError)
  {
    super(taskID, AUDIT_DATA_SECURITY_TASK_CLASS, scheduledStartTime,
         dependencyIDs, failedDependencyAction, notifyOnCompletion,
         notifyOnError);

    this.includeAuditors = getStringList(includeAuditors);
    this.excludeAuditors = getStringList(excludeAuditors);
    this.backendIDs      = getStringList(backendIDs);
    this.reportFilters   = getStringList(reportFilters);
    this.outputDirectory = outputDirectory;

    ensureTrue(
         (this.includeAuditors.isEmpty() || this.excludeAuditors.isEmpty()),
         "You cannot request both include and exclude auditors.");
  }



  /**
   * Creates a new audit data security task from the provided entry.
   *
   * @param  entry  The entry to use to create this audit data security task.
   *
   * @throws  TaskException  If the provided entry cannot be parsed as an audit
   *                         data security task entry.
   */
  public AuditDataSecurityTask(final Entry entry)
         throws TaskException
  {
    super(entry);

    includeAuditors = Collections.unmodifiableList(StaticUtils.toNonNullList(
         entry.getAttributeValues(ATTR_INCLUDE_AUDITOR)));
    excludeAuditors = Collections.unmodifiableList(StaticUtils.toNonNullList(
         entry.getAttributeValues(ATTR_EXCLUDE_AUDITOR)));
    backendIDs = Collections.unmodifiableList(StaticUtils.toNonNullList(
         entry.getAttributeValues(ATTR_BACKEND_ID)));
    reportFilters = Collections.unmodifiableList(StaticUtils.toNonNullList(
         entry.getAttributeValues(ATTR_REPORT_FILTER)));
    outputDirectory = entry.getAttributeValue(ATTR_OUTPUT_DIRECTORY);
  }



  /**
   * Creates a new audit data security task from the provided set of task
   * properties.
   *
   * @param  properties  The set of task properties and their corresponding
   *                     values to use for the task.  It must not be
   *                     {@code null}.
   *
   * @throws  TaskException  If the provided set of properties cannot be used to
   *                         create a valid audit data security task.
   */
  public AuditDataSecurityTask(final Map<TaskProperty,List<Object>> properties)
         throws TaskException
  {
    super(AUDIT_DATA_SECURITY_TASK_CLASS, properties);

    String outputDir = null;
    final LinkedList<String> includeAuditorsList = new LinkedList<String>();
    final LinkedList<String> excludeAuditorsList = new LinkedList<String>();
    final LinkedList<String> backendIDList       = new LinkedList<String>();
    final LinkedList<String> reportFilterList    = new LinkedList<String>();
    for (final Map.Entry<TaskProperty,List<Object>> entry :
         properties.entrySet())
    {
      final TaskProperty p = entry.getKey();
      final String attrName = StaticUtils.toLowerCase(p.getAttributeName());
      final List<Object> values = entry.getValue();

      if (attrName.equals(ATTR_INCLUDE_AUDITOR))
      {
        final String[] s = parseStrings(p, values, null);
        if (s != null)
        {
          includeAuditorsList.addAll(Arrays.asList(s));
        }
      }
      else if (attrName.equals(ATTR_EXCLUDE_AUDITOR))
      {
        final String[] s = parseStrings(p, values, null);
        if (s != null)
        {
          excludeAuditorsList.addAll(Arrays.asList(s));
        }
      }
      else if (attrName.equals(ATTR_BACKEND_ID))
      {
        final String[] s = parseStrings(p, values, null);
        if (s != null)
        {
          backendIDList.addAll(Arrays.asList(s));
        }
      }
      else if (attrName.equals(ATTR_REPORT_FILTER))
      {
        final String[] s = parseStrings(p, values, null);
        if (s != null)
        {
          reportFilterList.addAll(Arrays.asList(s));
        }
      }
      else if (attrName.equals(ATTR_OUTPUT_DIRECTORY))
      {
        outputDir = parseString(p, values, null);
      }
    }

    includeAuditors = Collections.unmodifiableList(includeAuditorsList);
    excludeAuditors = Collections.unmodifiableList(excludeAuditorsList);
    backendIDs      = Collections.unmodifiableList(backendIDList);
    reportFilters   = Collections.unmodifiableList(reportFilterList);
    outputDirectory = outputDir;

    if ((! includeAuditors.isEmpty()) && (! excludeAuditors.isEmpty()))
    {
      throw new TaskException(
           ERR_AUDIT_DATA_SECURITY_BOTH_INCLUDE_AND_EXCLUDE_AUDITORS.get());
    }
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getTaskName()
  {
    return INFO_TASK_NAME_AUDIT_DATA_SECURITY.get();
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getTaskDescription()
  {
    return INFO_TASK_DESCRIPTION_AUDIT_DATA_SECURITY.get();
  }



  /**
   * Retrieves the names of the auditors that should be invoked during the
   * data security audit.
   *
   * @return  The names of the include auditors that should be used for the
   *          task, or an empty list if either an exclude list should be used or
   *          all enabled auditors should be used.
   */
  public List<String> getIncludeAuditors()
  {
    return includeAuditors;
  }



  /**
   * Retrieves the names of the auditors that should not be invoked during the
   * audit.
   *
   * @return  The names of the exclude auditors that should be used for the
   *          task, or an empty list if either an include list should be used or
   *          all enabled auditors should be used.
   */
  public List<String> getExcludeAuditors()
  {
    return excludeAuditors;
  }



  /**
   * Retrieves the backend IDs of the backends that should be examined during
   * the course of the audit.
   *
   * @return  The backend IDs of the backends that should be examined during the
   *          course of the audit, or an empty list if all backends that support
   *          this capability should be used.
   */
  public List<String> getBackendIDs()
  {
    return backendIDs;
  }



  /**
   * Retrieves the string representations of the report filters that should be
   * used to identify which entries should be examined during the course of the
   * audit.
   *
   * @return  The string representations of the report filters that should be
   *          used to identify which entries should be examined during the
   *          course of the audit, or an empty list if all entries should be
   *          examined.
   */
  public List<String> getReportFilterStrings()
  {
    return reportFilters;
  }



  /**
   * Retrieves the parsed report filters that should be used to identify which
   * entries should be examined during the course of the audit.
   *
   * @return  The parsed report filters that should be used to identify which
   *          entries should be examined during the course of the audit, or an
   *          empty list if all entries should be examined.
   *
   * @throws  LDAPException  If any of the filter strings cannot be parsed as a
   *                         valid filter.
   */
  public List<Filter> getReportFilters()
         throws LDAPException
  {
    if (reportFilters.isEmpty())
    {
      return Collections.emptyList();
    }

    final ArrayList<Filter> filterList =
         new ArrayList<Filter>(reportFilters.size());
    for (final String filter : reportFilters)
    {
      filterList.add(Filter.create(filter));
    }
    return Collections.unmodifiableList(filterList);
  }



  /**
   * Retrieves the path to the directory on the server filesystem in which the
   * report output files should be written.
   *
   * @return  The path to the directory on the server filesystem in which the
   *          report output files should be written.
   */
  public String getOutputDirectory()
  {
    return outputDirectory;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  protected List<String> getAdditionalObjectClasses()
  {
    return Arrays.asList(OC_AUDIT_DATA_SECURITY_TASK);
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  protected List<Attribute> getAdditionalAttributes()
  {
    final LinkedList<Attribute> attrList = new LinkedList<Attribute>();

    if (! includeAuditors.isEmpty())
    {
      attrList.add(new Attribute(ATTR_INCLUDE_AUDITOR, includeAuditors));
    }

    if (! excludeAuditors.isEmpty())
    {
      attrList.add(new Attribute(ATTR_EXCLUDE_AUDITOR, excludeAuditors));
    }

    if (! backendIDs.isEmpty())
    {
      attrList.add(new Attribute(ATTR_BACKEND_ID, backendIDs));
    }

    if (! reportFilters.isEmpty())
    {
      attrList.add(new Attribute(ATTR_REPORT_FILTER, reportFilters));
    }

    if (outputDirectory != null)
    {
      attrList.add(new Attribute(ATTR_OUTPUT_DIRECTORY, outputDirectory));
    }

    return attrList;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public List<TaskProperty> getTaskSpecificProperties()
  {
    return Collections.unmodifiableList(Arrays.asList(
         PROPERTY_INCLUDE_AUDITOR,
         PROPERTY_EXCLUDE_AUDITOR,
         PROPERTY_BACKEND_ID,
         PROPERTY_REPORT_FILTER,
         PROPERTY_OUTPUT_DIRECTORY));
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public Map<TaskProperty,List<Object>> getTaskPropertyValues()
  {
    final LinkedHashMap<TaskProperty,List<Object>> props =
         new LinkedHashMap<TaskProperty,List<Object>>(5);

    if (! includeAuditors.isEmpty())
    {
      props.put(PROPERTY_INCLUDE_AUDITOR,
           Collections.<Object>unmodifiableList(includeAuditors));
    }

    if (! excludeAuditors.isEmpty())
    {
      props.put(PROPERTY_EXCLUDE_AUDITOR,
           Collections.<Object>unmodifiableList(excludeAuditors));
    }

    if (! backendIDs.isEmpty())
    {
      props.put(PROPERTY_BACKEND_ID,
           Collections.<Object>unmodifiableList(backendIDs));
    }

    if (! reportFilters.isEmpty())
    {
      props.put(PROPERTY_REPORT_FILTER,
           Collections.<Object>unmodifiableList(reportFilters));
    }

    if (outputDirectory != null)
    {
      props.put(PROPERTY_OUTPUT_DIRECTORY,
           Collections.<Object>unmodifiableList(Arrays.asList(
                outputDirectory)));
    }

    return Collections.unmodifiableMap(props);
  }



  /**
   * Retrieves an unmodifiable list using information from the provided list.
   * If the given list is {@code null}, then an empty list will be returned.
   * Otherwise, an unmodifiable version of the provided list will be returned.
   *
   * @param  l  The list to be processed.
   *
   * @return  The resulting string list.
   */
  private static List<String> getStringList(final List<String> l)
  {
    if (l == null)
    {
      return Collections.emptyList();
    }
    else
    {
      return Collections.unmodifiableList(l);
    }
  }
}
