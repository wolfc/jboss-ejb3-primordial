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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class EJBProxy implements InvocationHandler
{
   private EJBManager manager;
   // you can have concurrent invocations
   private ThreadLocal<Object> instance = new ThreadLocal<Object>();

   public EJBProxy(EJBManager manager)
   {
      this.manager = manager;
   }

   protected Object getInstance()
   {
      Object current = instance.get();
      if(current == null)
         throw new IllegalStateException("No instance associated");
      return current;
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      return manager.invoke(proxy, method, args);
   }

   protected void removeInstance()
   {
      Object current = instance.get();
      if(current == null)
         throw new IllegalStateException("No instance associated");
      // set(null) is faster than remove
      instance.set(null);
   }

   protected void setInstance(Object o)
   {
      Object current = instance.get();
      if(current != null)
         throw new IllegalStateException("Instance already associated");
      instance.set(o);
   }
}
