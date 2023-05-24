package com.example.demo;

import java.util.EnumSet;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class DemoApplication {

	@PersistenceContext
	private EntityManager entityManager;

	private final PlatformTransactionManager transactionManager;

	public DemoApplication(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public static void main(String[] args) {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
			context.register(AppConfiguration.class, DemoApplication.class);
			context.refresh();
			DemoApplication demo = context.getBean(DemoApplication.class);
			demo.createUsers();
			demo.deleteUsers();
		}
	}

	private void deleteUsers() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.execute((status) -> {
			for (User user : this.entityManager.createQuery("Select u from User u", User.class).getResultList()) {
				this.entityManager.remove(user);
			}
			return null;
		});
	}

	private void createUsers() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.execute((status) -> {
			User a = new User();
			a.setUserRoles(EnumSet.of(UserRole.USER));
			this.entityManager.persist(a);
			User user = new User();
			user.setUserRoles(EnumSet.of(UserRole.USER));
			user.setCreatedBy(a);
			this.entityManager.persist(user);
			System.out.println(user.getCreatedBy().getId());
			return null;
		});
	}

	@Configuration(proxyBeanMethods = false)
	static class AppConfiguration {

		@Bean
		DataSource dataSource() {
			HikariDataSource dataSource = new HikariDataSource();
			dataSource.setDriverClassName("org.h2.Driver");
			dataSource.setJdbcUrl("jdbc:h2:mem:test");
			return dataSource;
		}

		@Bean
		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean(DataSource dataSource) {
			LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
			factory.setDataSource(dataSource);
			factory.setJpaDialect(new HibernateJpaDialect());
			factory.setPackagesToScan("com.example.demo");
			factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
			factory.setJpaPropertyMap(Map.of(
					AvailableSettings.HBM2DDL_AUTO, "update", 
					AvailableSettings.SHOW_SQL, "true"
			));
			return factory;
		}

		@Bean
		PlatformTransactionManager transactionManager() {
			return new JpaTransactionManager();
		}

	}

}
