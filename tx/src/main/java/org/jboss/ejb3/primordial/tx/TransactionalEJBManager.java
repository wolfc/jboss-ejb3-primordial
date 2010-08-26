/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ejb3.primordial.tx;

import javax.ejb.ApplicationException;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagementType;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public interface TransactionalEJBManager
{
   ApplicationException getApplicationException(Class<? extends Throwable> aClass);
   
   String getName();

   TransactionAttributeType getTransactionAttributeType(Method method);
   
   TransactionManagementType getTransactionManagementType();

   int getTransactionTimeout(InvocationContext invocation);
   
   // TODO: inherit from EJBManager
   Object invoke(Object session, Method method, Object args[]) throws Exception;

   // TODO: inherit
   boolean isStateful();
}
