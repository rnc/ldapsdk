/*
 * Copyright 2010-2016 UnboundID Corp.
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



import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.Entry;
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
 * server to generate administrative alerts, or to manage the set of degraded or
 * unavailable alert types.
 * <BR><BR>
 * The properties that are available for use with this type of task include:
 * <UL>
 *   <LI>The alert type of the alert notification to generate.  If this is
 *       provided, then an alert message must also be provided.</LI>
 *   <LI>The alert message for the alert notification to generate.  If this is
 *       provided, then an alert type must also be provided.</LI>
 *   <LI>The names of the alert types to add to the set of degraded alert types
 *       in the general monitor entry.</LI>
 *   <LI>The names of the alert types to remove from the set of degraded alert
 *       types in the general monitor entry.</LI>
 *   <LI>The names of the alert types to add to the set of unavailable alert
 *       types in the general monitor entry.</LI>
 *   <LI>The names of the alert types to remove from the set of unavailable
 *       alert types in the general monitor entry.</LI>
 * </UL>
 */
@NotMutable()
@ThreadSafety(level=ThreadSafetyLevel.COMPLETELY_THREADSAFE)
public final class AlertTask
       extends Task
{
  /**
   * The fully-qualified name of the Java class that is used for the alert task.
   */
  static final String ALERT_TASK_CLASS =
       "com.unboundid.directory.server.tasks.AlertTask";



  /**
   * The name of the attribute used to specify the alert type for the alert to
   * generate.
   */
  private static final String ATTR_ALERT_TYPE = "ds-task-alert-type";



  /**
   * The name of the attribute used to specify the message for the alert to
   * generate.
   */
  private static final String ATTR_ALERT_MESSAGE = "ds-task-alert-message";



  /**
   * The name of the attribute used to specify the alert type(s) to add to the
   * set of degraded alert types.
   */
  private static final String ATTR_ADD_DEGRADED_TYPE =
       "ds-task-alert-add-degraded-type";



  /**
   * The name of the attribute used to specify the alert type(s) to remove from
   * the set of degraded alert types.
   */
  private static final String ATTR_REMOVE_DEGRADED_TYPE =
       "ds-task-alert-remove-degraded-type";



  /**
   * The name of the attribute used to specify the alert type(s) to add to the
   * set of unavailable alert types.
   */
  private static final String ATTR_ADD_UNAVAILABLE_TYPE =
       "ds-task-alert-add-unavailable-type";



  /**
   * The name of the attribute used to specify the alert type(s) to remove from
   * the set of unavailable alert types.
   */
  private static final String ATTR_REMOVE_UNAVAILABLE_TYPE =
       "ds-task-alert-remove-unavailable-type";



  /**
   * The name of the object class used in alert task entries.
   */
  private static final String OC_ALERT_TASK = "ds-task-alert";



  /**
   * The task property that will be used for the alert type.
   */
  private static final TaskProperty PROPERTY_ALERT_TYPE =
     new TaskProperty(ATTR_ALERT_TYPE, INFO_ALERT_DISPLAY_NAME_TYPE.get(),
          INFO_ALERT_DESCRIPTION_TYPE.get(), String.class, false, false,
          false);



  /**
   * The task property that will be used for the alert message.
   */
  private static final TaskProperty PROPERTY_ALERT_MESSAGE =
     new TaskProperty(ATTR_ALERT_MESSAGE, INFO_ALERT_DISPLAY_NAME_MESSAGE.get(),
          INFO_ALERT_DESCRIPTION_MESSAGE.get(), String.class, false, false,
          false);



  /**
   * The task property that will be used for the add degraded alert types.
   */
  private static final TaskProperty PROPERTY_ADD_DEGRADED_TYPE =
     new TaskProperty(ATTR_ADD_DEGRADED_TYPE,
          INFO_ALERT_DISPLAY_NAME_ADD_DEGRADED.get(),
          INFO_ALERT_DESCRIPTION_ADD_DEGRADED.get(), String.class, false, true,
          false);



  /**
   * The task property that will be used for the remove degraded alert types.
   */
  private static final TaskProperty PROPERTY_REMOVE_DEGRADED_TYPE =
     new TaskProperty(ATTR_REMOVE_DEGRADED_TYPE,
          INFO_ALERT_DISPLAY_NAME_REMOVE_DEGRADED.get(),
          INFO_ALERT_DESCRIPTION_REMOVE_DEGRADED.get(), String.class, false,
          true, false);



  /**
   * The task property that will be used for the add unavailable alert types.
   */
  private static final TaskProperty PROPERTY_ADD_UNAVAILABLE_TYPE =
     new TaskProperty(ATTR_ADD_UNAVAILABLE_TYPE,
          INFO_ALERT_DISPLAY_NAME_ADD_UNAVAILABLE.get(),
          INFO_ALERT_DESCRIPTION_ADD_UNAVAILABLE.get(), String.class, false,
          true, false);



  /**
   * The task property that will be used for the remove unavailable alert types.
   */
  private static final TaskProperty PROPERTY_REMOVE_UNAVAILABLE_TYPE =
     new TaskProperty(ATTR_REMOVE_UNAVAILABLE_TYPE,
          INFO_ALERT_DISPLAY_NAME_REMOVE_UNAVAILABLE.get(),
          INFO_ALERT_DESCRIPTION_REMOVE_UNAVAILABLE.get(), String.class, false,
          true, false);



  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = 8253375533166941221L;



  // The alert types to add to the set of degraded alert types.
  private final List<String> addDegradedTypes;

  // The alert types to add to the set of unavailable alert types.
  private final List<String> addUnavailableTypes;

  // The alert types to remove from the set of degraded alert types.
  private final List<String> removeDegradedTypes;

  // The alert types to remove from the set of unavailable alert types.
  private final List<String> removeUnavailableTypes;

  // The message for the alert to be generated.
  private final String alertMessage;

  // The name of the alert type for the alert to be generated.
  private final String alertType;



  /**
   * Creates a new uninitialized alert task instance which should only be used
   * for obtaining general information about this task, including the task name,
   * description, and supported properties.  Attempts to use a task created with
   * this constructor for any other reason will likely fail.
   */
  public AlertTask()
  {
    alertType              = null;
    alertMessage           = null;
    addDegradedTypes       = null;
    addUnavailableTypes    = null;
    removeDegradedTypes    = null;
    removeUnavailableTypes = null;
  }




  /**
   * Creates a new alert task that can be used to generate an administrative
   * alert with the provided information.
   *
   * @param  alertType     The alert type to use for the generated alert.  It
   *                       must not be {@code null}.
   * @param  alertMessage  The message to use for the generated alert.  It must
   *                       not be {@code null}.
   */
  public AlertTask(final String alertType, final String alertMessage)
  {
    this(null, alertType, alertMessage, null, null, null, null, null, null,
         null, null, null);
  }



  /**
   * Creates a new alert task that can be used to generate an administrative
   * alert and/or update the set of degraded or unavailable alert types for the
   * Directory Server.  At least one element must be provided.
   *
   * @param  alertType               The alert type to use for the generated
   *                                 alert.  It may be {@code null} if no alert
   *                                 should be generated, but if it is
   *                                 non-{@code null} then the alert message
   *                                 must also be non-{@code null}.
   * @param  alertMessage            The message to use for the generated alert.
   *                                 It may be {@code null} if no alert should
   *                                 be generated, but if it is non-{@code null}
   *                                 then the alert type must also be
   *                                 non-{@code null}.
   * @param  addDegradedTypes        The names of the alert types to add to the
   *                                 Directory Server's set of degraded alert
   *                                 types.  It may be {@code null} or empty if
   *                                 no degraded alert types should be added.
   * @param  removeDegradedTypes     The names of the alert types to remove from
   *                                 the Directory Server's set of degraded
   *                                 alert types.  It may be {@code null} or
   *                                 empty if no degraded alert types should be
   *                                 removed.
   * @param  addUnavailableTypes     The names of the alert types to add to the
   *                                 Directory Server's set of unavailable alert
   *                                 types.  It may be {@code null} or empty if
   *                                 no unavailable alert types should be added.
   * @param  removeUnavailableTypes  The names of the alert types to remove from
   *                                 the Directory Server's set of unavailable
   *                                 alert types.  It may be {@code null} or
   *                                 empty if no unavailable alert types should
   *                                 be removed.
   */
  public AlertTask(final String alertType, final String alertMessage,
                   final List<String> addDegradedTypes,
                   final List<String> removeDegradedTypes,
                   final List<String> addUnavailableTypes,
                   final List<String> removeUnavailableTypes)
  {
    this(null, alertType, alertMessage, addDegradedTypes, removeDegradedTypes,
         addUnavailableTypes, removeUnavailableTypes, null, null, null,
         null, null);
  }



  /**
   * Creates a new alert task that can be used to generate an administrative
   * alert and/or update the set of degraded or unavailable alert types for the
   * Directory Server.  At least one alert-related element must be provided.
   *
   * @param  taskID                  The task ID to use for this task.  If it is
   *                                 {@code null} then a UUID will be generated
   *                                 for use as the task ID.
   * @param  alertType               The alert type to use for the generated
   *                                 alert.  It may be {@code null} if no alert
   *                                 should be generated, but if it is
   *                                 non-{@code null} then the alert message
   *                                 must also be non-{@code null}.
   * @param  alertMessage            The message to use for the generated alert.
   *                                 It may be {@code null} if no alert should
   *                                 be generated, but if it is non-{@code null}
   *                                 then the alert type must also be
   *                                 non-{@code null}.
   * @param  addDegradedTypes        The names of the alert types to add to the
   *                                 Directory Server's set of degraded alert
   *                                 types.  It may be {@code null} or empty if
   *                                 no degraded alert types should be added.
   * @param  removeDegradedTypes     The names of the alert types to remove from
   *                                 the Directory Server's set of degraded
   *                                 alert types.  It may be {@code null} or
   *                                 empty if no degraded alert types should be
   *                                 removed.
   * @param  addUnavailableTypes     The names of the alert types to add to the
   *                                 Directory Server's set of unavailable alert
   *                                 types.  It may be {@code null} or empty if
   *                                 no unavailable alert types should be added.
   * @param  removeUnavailableTypes  The names of the alert types to remove from
   *                                 the Directory Server's set of unavailable
   *                                 alert types.  It may be {@code null} or
   *                                 empty if no unavailable alert types should
   *                                 be removed.
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
  public AlertTask(final String taskID, final String alertType,
                   final String alertMessage,
                   final List<String> addDegradedTypes,
                   final List<String> removeDegradedTypes,
                   final List<String> addUnavailableTypes,
                   final List<String> removeUnavailableTypes,
                   final Date scheduledStartTime,
                   final List<String> dependencyIDs,
                   final FailedDependencyAction failedDependencyAction,
                   final List<String> notifyOnCompletion,
                   final List<String> notifyOnError)
  {
    super(taskID, ALERT_TASK_CLASS, scheduledStartTime, dependencyIDs,
         failedDependencyAction, notifyOnCompletion, notifyOnError);

    this.alertType    = alertType;
    this.alertMessage = alertMessage;

    ensureTrue((alertType == null) == (alertMessage == null));


    this.addDegradedTypes       = getStringList(addDegradedTypes);
    this.removeDegradedTypes    = getStringList(removeDegradedTypes);
    this.addUnavailableTypes    = getStringList(addUnavailableTypes);
    this.removeUnavailableTypes = getStringList(removeUnavailableTypes);

    if (alertType == null)
    {
      ensureFalse(this.addDegradedTypes.isEmpty() &&
           this.removeDegradedTypes.isEmpty() &&
           this.addUnavailableTypes.isEmpty() &&
           this.removeUnavailableTypes.isEmpty());
    }
  }



  /**
   * Creates a new alert task from the provided entry.
   *
   * @param  entry  The entry to use to create this alert task.
   *
   * @throws  TaskException  If the provided entry cannot be parsed as an alert
   *                         task entry.
   */
  public AlertTask(final Entry entry)
         throws TaskException
  {
    super(entry);


    // Get the alert type and message.  If either is present, then both must be.
    alertType    = entry.getAttributeValue(ATTR_ALERT_TYPE);
    alertMessage = entry.getAttributeValue(ATTR_ALERT_MESSAGE);

    if ((alertType == null) != (alertMessage == null))
    {
      throw new TaskException(ERR_ALERT_TYPE_AND_MESSAGE_INTERDEPENDENT.get());
    }


    // Get the values to add/remove from the degraded/unavailable alert types.
    addDegradedTypes       = parseStringList(entry, ATTR_ADD_DEGRADED_TYPE);
    removeDegradedTypes    = parseStringList(entry, ATTR_REMOVE_DEGRADED_TYPE);
    addUnavailableTypes    = parseStringList(entry, ATTR_ADD_UNAVAILABLE_TYPE);
    removeUnavailableTypes = parseStringList(entry,
         ATTR_REMOVE_UNAVAILABLE_TYPE);

    if ((alertType == null) && addDegradedTypes.isEmpty() &&
        removeDegradedTypes.isEmpty() && addUnavailableTypes.isEmpty() &&
        removeUnavailableTypes.isEmpty())
    {
      throw new TaskException(ERR_ALERT_ENTRY_NO_ELEMENTS.get());
    }
  }



  /**
   * Creates a new alert task from the provided set of task properties.
   *
   * @param  properties  The set of task properties and their corresponding
   *                     values to use for the task.  It must not be
   *                     {@code null}.
   *
   * @throws  TaskException  If the provided set of properties cannot be used to
   *                         create a valid alert task.
   */
  public AlertTask(final Map<TaskProperty,List<Object>> properties)
         throws TaskException
  {
    super(ALERT_TASK_CLASS, properties);

    String type = null;
    String message = null;
    final LinkedList<String> addDegraded = new LinkedList<String>();
    final LinkedList<String> removeDegraded = new LinkedList<String>();
    final LinkedList<String> addUnavailable = new LinkedList<String>();
    final LinkedList<String> removeUnavailable = new LinkedList<String>();
    for (final Map.Entry<TaskProperty,List<Object>> entry :
         properties.entrySet())
    {
      final TaskProperty p = entry.getKey();
      final String attrName = StaticUtils.toLowerCase(p.getAttributeName());
      final List<Object> values = entry.getValue();

      if (attrName.equals(ATTR_ALERT_TYPE))
      {
        type = parseString(p, values, type);
      }
      else if (attrName.equals(ATTR_ALERT_MESSAGE))
      {
        message = parseString(p, values, message);
      }
      else if (attrName.equals(ATTR_ADD_DEGRADED_TYPE))
      {
        final String[] s = parseStrings(p, values, null);
        if (s != null)
        {
          addDegraded.addAll(Arrays.asList(s));
        }
      }
      else if (attrName.equals(ATTR_REMOVE_DEGRADED_TYPE))
      {
        final String[] s = parseStrings(p, values, null);
        if (s != null)
        {
          removeDegraded.addAll(Arrays.asList(s));
        }
      }
      else if (attrName.equals(ATTR_ADD_UNAVAILABLE_TYPE))
      {
        final String[] s = parseStrings(p, values, null);
        if (s != null)
        {
          addUnavailable.addAll(Arrays.asList(s));
        }
      }
      else if (attrName.equals(ATTR_REMOVE_UNAVAILABLE_TYPE))
      {
        final String[] s = parseStrings(p, values, null);
        if (s != null)
        {
          removeUnavailable.addAll(Arrays.asList(s));
        }
      }
    }

    alertType              = type;
    alertMessage           = message;
    addDegradedTypes       = Collections.unmodifiableList(addDegraded);
    removeDegradedTypes    = Collections.unmodifiableList(removeDegraded);
    addUnavailableTypes    = Collections.unmodifiableList(addUnavailable);
    removeUnavailableTypes = Collections.unmodifiableList(removeUnavailable);

    if ((alertType == null) != (alertMessage == null))
    {
      throw new TaskException(ERR_ALERT_TYPE_AND_MESSAGE_INTERDEPENDENT.get());
    }

    if ((alertType == null) && addDegradedTypes.isEmpty() &&
        removeDegradedTypes.isEmpty() && addUnavailableTypes.isEmpty() &&
        removeUnavailableTypes.isEmpty())
    {
      throw new TaskException(ERR_ALERT_PROPERTIES_NO_ELEMENTS.get());
    }
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getTaskName()
  {
    return INFO_TASK_NAME_ALERT.get();
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getTaskDescription()
  {
    return INFO_TASK_DESCRIPTION_ALERT.get();
  }



  /**
   * Retrieves the name of the alert type to use for the alert notification to
   * be generated, if appropriate.
   *
   * @return  The name of the alert type to use for the alert notification to be
   *          generated, or {@code null} if no alert should be generated.
   */
  public String getAlertType()
  {
    return alertType;
  }



  /**
   * Retrieves the message to use for the alert notification to be generated, if
   * appropriate.
   *
   * @return  The message to use for the alert notification to be generated, or
   *          {@code null} if no alert should be generated.
   */
  public String getAlertMessage()
  {
    return alertMessage;
  }



  /**
   * Retrieves the names of the alert types that should be added to the set of
   * degraded alert types.
   *
   * @return  The names of the alert types that should be added to the set of
   *          degraded alert types, or an empty list if no degraded alert types
   *          should be added.
   */
  public List<String> getAddDegradedAlertTypes()
  {
    return addDegradedTypes;
  }



  /**
   * Retrieves the names of the alert types that should be removed from the set
   * of degraded alert types.
   *
   * @return  The names of the alert types that should be removed from the set
   *          of degraded alert types, or an empty list if no degraded alert
   *          types should be removed.
   */
  public List<String> getRemoveDegradedAlertTypes()
  {
    return removeDegradedTypes;
  }



  /**
   * Retrieves the names of the alert types that should be added to the set of
   * unavailable alert types.
   *
   * @return  The names of the alert types that should be added to the set of
   *          unavailable alert types, or an empty list if no unavailable alert
   *          types should be added.
   */
  public List<String> getAddUnavailableAlertTypes()
  {
    return addUnavailableTypes;
  }



  /**
   * Retrieves the names of the alert types that should be removed from the set
   * of unavailable alert types.
   *
   * @return  The names of the alert types that should be removed from the set
   *          of unavailable alert types, or an empty list if no unavailable
   *          alert types should be removed.
   */
  public List<String> getRemoveUnavailableAlertTypes()
  {
    return removeUnavailableTypes;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  protected List<String> getAdditionalObjectClasses()
  {
    return Arrays.asList(OC_ALERT_TASK);
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  protected List<Attribute> getAdditionalAttributes()
  {
    final LinkedList<Attribute> attrList = new LinkedList<Attribute>();

    if (alertType != null)
    {
      attrList.add(new Attribute(ATTR_ALERT_TYPE, alertType));
      attrList.add(new Attribute(ATTR_ALERT_MESSAGE, alertMessage));
    }

    if (! addDegradedTypes.isEmpty())
    {
      attrList.add(new Attribute(ATTR_ADD_DEGRADED_TYPE, addDegradedTypes));
    }

    if (! removeDegradedTypes.isEmpty())
    {
      attrList.add(new Attribute(ATTR_REMOVE_DEGRADED_TYPE,
           removeDegradedTypes));
    }

    if (! addUnavailableTypes.isEmpty())
    {
      attrList.add(new Attribute(ATTR_ADD_UNAVAILABLE_TYPE,
           addUnavailableTypes));
    }

    if (! removeUnavailableTypes.isEmpty())
    {
      attrList.add(new Attribute(ATTR_REMOVE_UNAVAILABLE_TYPE,
           removeUnavailableTypes));
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
         PROPERTY_ALERT_TYPE, PROPERTY_ALERT_MESSAGE,
         PROPERTY_ADD_DEGRADED_TYPE, PROPERTY_REMOVE_DEGRADED_TYPE,
         PROPERTY_ADD_UNAVAILABLE_TYPE, PROPERTY_REMOVE_UNAVAILABLE_TYPE));
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public Map<TaskProperty,List<Object>> getTaskPropertyValues()
  {
    final LinkedHashMap<TaskProperty,List<Object>> props =
         new LinkedHashMap<TaskProperty,List<Object>>(6);

    if (alertType != null)
    {
      props.put(PROPERTY_ALERT_TYPE,
           Collections.<Object>unmodifiableList(Arrays.asList(alertType)));
      props.put(PROPERTY_ALERT_MESSAGE,
           Collections.<Object>unmodifiableList(Arrays.asList(alertMessage)));
    }

    if (! addDegradedTypes.isEmpty())
    {
      props.put(PROPERTY_ADD_DEGRADED_TYPE,
           Collections.<Object>unmodifiableList(addDegradedTypes));
    }

    if (! removeDegradedTypes.isEmpty())
    {
      props.put(PROPERTY_REMOVE_DEGRADED_TYPE,
           Collections.<Object>unmodifiableList(removeDegradedTypes));
    }

    if (! addUnavailableTypes.isEmpty())
    {
      props.put(PROPERTY_ADD_UNAVAILABLE_TYPE,
           Collections.<Object>unmodifiableList(addUnavailableTypes));
    }

    if (! removeUnavailableTypes.isEmpty())
    {
      props.put(PROPERTY_REMOVE_UNAVAILABLE_TYPE,
           Collections.<Object>unmodifiableList(removeUnavailableTypes));
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
