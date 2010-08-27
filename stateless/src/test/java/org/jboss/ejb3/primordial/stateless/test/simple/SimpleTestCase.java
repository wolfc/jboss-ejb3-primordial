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
package org.jboss.ejb3.primordial.stateless.test.simple;

import org.jboss.ejb3.primordial.stateless.test.common.DummyStatelessManager;
import org.jboss.ejb3.primordial.stateless.test.common.EJBManager;
import org.jboss.ejb3.primordial.stateless.test.common.EJBProxy;
import org.jboss.ejb3.primordial.stateless.test.common.PoolInterceptor;
import org.jboss.ejb3.primordial.stateless.test.common.TargetInvocationHandler;
import org.jboss.ejb3.sis.Interceptor;
import org.jboss.ejb3.sis.reflect.InterceptorInvocationHandler;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class SimpleTestCase
{
   @Test
   public void test1() throws Exception
   {
      DummyStatelessManager managerInstance = new DummyStatelessManager(GreeterBean.class);
      InvocationHandler handler = new TargetInvocationHandler(managerInstance);

      //InterceptorAssembly interceptor = new InterceptorAssembly()
      // as per annotation
      Interceptor interceptor = new PoolInterceptor();
      InterceptorInvocationHandler interceptorHandler = new InterceptorInvocationHandler(handler, interceptor);

      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class<?> interfaces[] = { EJBManager.class };
      EJBManager manager = (EJBManager) Proxy.newProxyInstance(loader, interfaces, interceptorHandler);

      EJBProxy ejbProxy = new EJBProxy(manager);
      
      Greeter greeter = (Greeter) Proxy.newProxyInstance(loader, new Class<?>[] { Greeter.class }, ejbProxy);
      String name = "Frankenstein";
      String result = greeter.sayHi(name);

      assertEquals("Hi Frankenstein", result);
   }
}
