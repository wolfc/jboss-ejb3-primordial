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
package org.jboss.ejb3.primordial.stateless.test.common;

import javax.annotation.ManagedBean;
import javax.interceptor.Interceptors;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
@ManagedBean
//@Lifecycle(Singleton.class)
public class DummyStatelessManager implements EJBManager
{
   private Class<?> beanClass;

   public DummyStatelessManager(Class<?> beanClass)
   {
      this.beanClass = beanClass;
   }
   
   public Class<?> getBeanClass()
   {
      return beanClass;
   }

   @Override
   @Interceptors({ PoolInterceptor.class })
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      EJBProxy handler = (EJBProxy) Proxy.getInvocationHandler(proxy);
      System.out.println("handler = " + handler);
      System.out.println("method = " + method);
      try
      {
         return method.invoke(handler.getInstance(), args);
      }
      catch(InvocationTargetException e)
      {
         throw e.getTargetException();
      }
   }
}
