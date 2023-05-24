package com.example.demo;

import java.util.EnumSet;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DemoApplication {

	private final EntityManagerFactory entityManagerFactory;

	public DemoApplication() {
		this.entityManagerFactory = Persistence.createEntityManagerFactory("test");
	}

	public static void main(String[] args) {
		DemoApplication demoApplication = new DemoApplication();
		demoApplication.createUsers();
		demoApplication.deleteUsers();
	}

	private void deleteUsers() {
		EntityManager entityManager = this.entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		for (User user : entityManager.createQuery("Select u from User u", User.class).getResultList()) {
			entityManager.remove(user);
		}
		entityManager.getTransaction().commit();
	}

	private void createUsers() {
		EntityManager entityManager = this.entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		User a = new User();
		a.setUserRoles(EnumSet.of(UserRole.USER));
		entityManager.persist(a);
		User user = new User();
		user.setUserRoles(EnumSet.of(UserRole.USER));
		user.setCreatedBy(a);
		entityManager.persist(user);
		entityManager.getTransaction().commit();
	}
}
