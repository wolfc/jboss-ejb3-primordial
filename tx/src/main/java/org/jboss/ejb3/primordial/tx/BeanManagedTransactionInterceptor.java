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

import org.jboss.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.TransactionManagementType;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import static org.jboss.ejb3.primordial.tx.util.StatusHelper.statusAsString;

/**
 * EJB 3 13.6.1:
 * In the case of a stateful session bean, it is possible that the business method that started a transaction
 * completes without committing or rolling back the transaction. In such a case, the container must retain
 * the association between the transaction and the instance across multiple client calls until the instance
 * commits or rolls back the transaction. When the client invokes the next business method, the container
 * must invoke the business method (and any applicable interceptor methods for the bean) in this transac-
 * tion context.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
@Interceptor
public class BeanManagedTransactionInterceptor
{
   private static final Logger log = Logger.getLogger(BeanManagedTransactionInterceptor.class);

   private ContainerManagedTransactionInterceptor cmt = new ContainerManagedTransactionInterceptor();

   private TransactionManager tm;

   private void checkBadStateful(String ejbName)
   {
      int status = Status.STATUS_NO_TRANSACTION;

      try
      {
         status = tm.getStatus();
      }
      catch (SystemException ex)
      {
         log.error("Failed to get status", ex);
      }

      switch (status)
      {
         case Status.STATUS_COMMITTING :
         case Status.STATUS_MARKED_ROLLBACK :
         case Status.STATUS_PREPARING :
         case Status.STATUS_ROLLING_BACK :
            try
            {
               tm.rollback();
            }
            catch (Exception ex)
            {
               log.error("Failed to rollback", ex);
            }
            String msg = "BMT stateful bean '" + ejbName
                         + "' did not complete user transaction properly status=" + statusAsString(status);
            log.error(msg);
      }
   }

   /**
    * Stateful BMT TX
    */
   protected Object handleInvocation(InvocationContext context) throws Exception
   {
      assert tm.getTransaction() == null : "can't handle BMT transaction, there is a transaction active";

      TransactionalEJBManager manager = (TransactionalEJBManager) context.getTarget();

      StatefulContext ctx = (StatefulContext) context.getParameters()[0];
      String ejbName = manager.getName();

      // Is the instance already associated with a transaction?
      Transaction tx = ctx.getTransaction();
      if (tx != null)
      {
         ctx.setTransaction(null);
         // then resume that transaction.
         tm.resume(tx);
      }
      try
      {
         return context.proceed();
      }
      finally
      {
         checkBadStateful(ejbName);
         // Is the instance finished with the transaction?
         Transaction newTx = tm.getTransaction();
         if (newTx != null)
         {
            // remember the association
            ctx.setTransaction(newTx);
            // and suspend it.
            tm.suspend();
         }
         else
         {
            // forget any previous associated transaction
            ctx.setTransaction(null);
         }
      }
   }

   @AroundInvoke
   public Object invoke(InvocationContext invocation) throws Exception
   {
      TransactionalEJBManager manager = (TransactionalEJBManager) invocation.getTarget();
      if(!manager.isStateful() || manager.getTransactionManagementType() != TransactionManagementType.BEAN)
         return invocation.proceed();
      Transaction oldTx = tm.suspend();
      try
      {
         return handleInvocation(invocation);
      }
      finally
      {
         if (oldTx != null) tm.resume(oldTx);
      }
   }

   @Resource
   public void setTransactionManager(TransactionManager tm)
   {
      this.tm = tm;
   }
}
