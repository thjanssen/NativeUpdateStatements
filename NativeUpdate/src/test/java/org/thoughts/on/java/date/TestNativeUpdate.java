package org.thoughts.on.java.date;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.thoughts.on.java.model.PersonEntity;

public class TestNativeUpdate {

	Logger log = Logger.getLogger(this.getClass().getName());

	private EntityManagerFactory emf;

	@Before
	public void init() {
		emf = Persistence.createEntityManagerFactory("my-persistence-unit");
	}

	@After
	public void close() {
		emf.close();
	}

	@Test
	public void testEntityUpdate() {
		log.info("... testEntityUpdate ...");

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		long start = System.currentTimeMillis();
		
		for (long i=0; i<200; i++) {
			PersonEntity p = em.find(PersonEntity.class, i);
			p.setFirstName(p.getLastName()+"-changed");
		}
		
		em.flush();
		long end = System.currentTimeMillis();
		System.out.println("Native Update: "+(end-start));
		
		em.getTransaction().commit();
		em.close();
	}
	
	@Test
	public void testNativeUpdate() {
		log.info("... testNativeUpdate ...");

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		long start = System.currentTimeMillis();
		
		em.createNativeQuery("UPDATE person p SET firstname = firstname || '-changed'").executeUpdate();
		
		em.flush();
		long end = System.currentTimeMillis();
		System.out.println("Native Update: "+(end-start));
		
		em.getTransaction().commit();
		em.close();
	}
	
	@Test
	public void testOutdated1stLevel() {
		log.info("... testOutdated1stLevel ...");

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		PersonEntity p = em.find(PersonEntity.class, 1L);
		
		em.createNativeQuery("UPDATE person p SET firstname = firstname || '-changed'").executeUpdate();
		
		log.info("FirstName: "+p.getFirstName());
		
		p = em.find(PersonEntity.class, 1L);
		log.info("FirstName: "+p.getFirstName());
		
		em.getTransaction().commit();
		em.close();
	}
	
	@Test
	public void testDetachEntity() {
		log.info("... testDetachEntity ...");

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		PersonEntity p = em.find(PersonEntity.class, 1L);
		
		log.info("Detach PersonEntity");
		em.flush();
		em.detach(p);
		
		em.createNativeQuery("UPDATE person p SET firstname = firstname || '-changed'").executeUpdate();
		
		p = em.find(PersonEntity.class, 1L);
		log.info("FirstName: "+p.getFirstName());
		
		em.getTransaction().commit();
		em.close();
	}
}
