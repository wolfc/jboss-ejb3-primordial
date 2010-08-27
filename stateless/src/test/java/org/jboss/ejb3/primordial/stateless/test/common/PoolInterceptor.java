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

import org.jboss.ejb3.sis.Interceptor;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Proxy;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class PoolInterceptor implements Interceptor
{
   public Object invoke(InvocationContext invocation) throws Exception
   {
      if(!invocation.getMethod().getName().equals("invoke"))
         return invocation.proceed();
      System.out.println("enter pool " + invocation.getMethod());
      System.out.println("  target = " + invocation.getTarget());
      System.out.println("  param[0] = " + invocation.getParameters()[0].getClass());
      EJBManager manager = (EJBManager) invocation.getTarget();
      EJBProxy handler = (EJBProxy) Proxy.getInvocationHandler(invocation.getParameters()[0]);
      Class<?> beanClass = manager.getBeanClass();
      Object instance = beanClass.newInstance();
      // injection, lifecycle interceptors, etc etc
      handler.setInstance(instance);
      try
      {
         return invocation.proceed();
      }
      finally
      {
         handler.removeInstance();
         System.out.println("exit pool");
      }
   }
}
