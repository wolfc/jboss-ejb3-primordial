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
package org.jboss.ejb3.primordial.tx.test.common;

import org.jboss.ejb3.primordial.tx.BeanManagedTransactionInterceptor;
import org.jboss.ejb3.primordial.tx.ContainerManagedTransactionInterceptor;
import org.jboss.ejb3.primordial.tx.TransactionalEJBManager;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.ejb.ApplicationException;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;

/**
 * A naive implementation that only functions on annotations, not metadata.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
@ManagedBean
public class DummyTransactionalEJBManager implements TransactionalEJBManager
{
   private String name;
   private Class<?> beanClass;
   
   @Override
   public ApplicationException getApplicationException(Class<? extends Throwable> aClass)
   {
      return aClass.getAnnotation(ApplicationException.class);
   }

   @Override
   public String getName()
   {
      return name;
   }

   private TransactionAttributeType getTransactionAttributeType()
   {
      TransactionAttribute attr = beanClass.getAnnotation(TransactionAttribute.class);
      if(attr != null)
         return attr.value();
      return TransactionAttributeType.REQUIRED;
   }

   @Override
   public TransactionAttributeType getTransactionAttributeType(Method method)
   {
      TransactionAttribute attr = method.getAnnotation(TransactionAttribute.class);
      if(attr != null)
         return attr.value();
      return getTransactionAttributeType();
   }

   @Override
   public TransactionManagementType getTransactionManagementType()
   {
      TransactionManagement txManagement = beanClass.getAnnotation(TransactionManagement.class);
      if(txManagement != null)
         return txManagement.value();
      return TransactionManagementType.CONTAINER;
   }

   @Override
   public int getTransactionTimeout(InvocationContext invocation)
   {
      // TODO: disabled for the moment
      return -1;
   }

   @Override
   // TODO: a PoolInterceptor or CacheInterceptor in between
   @Interceptors({ ContainerManagedTransactionInterceptor.class, BeanManagedTransactionInterceptor.class })
   public Object invoke(Object session, Method method, Object[] args) throws Exception
   {
      // Object target = ((EJBContext) session).getTarget();
      Object target = session;
      return method.invoke(target, args);
   }

   @Override
   public boolean isStateful()
   {
      return beanClass.getAnnotation(Stateful.class) != null;
   }
   
   @Resource
   public void setBeanClass(Class<?> cls)
   {
      this.beanClass = cls;
   }

   @Resource
   public void setName(String s)
   {
      this.name = s;
   }
}
