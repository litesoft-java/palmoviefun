package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.MoviesBean;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Objects;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    DatabaseServiceCredentials credentialsProvider(@Value("${VCAP_SERVICES}") String vcapServicesJson) {
        vcapServicesJson = Objects.requireNonNull(vcapServicesJson, "VCAP_SERVICES not set").trim();
        if (vcapServicesJson.isEmpty()) {
            throw new IllegalStateException("VCAP_SERVICES empty");
        }
        return new DatabaseServiceCredentials(vcapServicesJson);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean moviesEntityManagerFactoryBean(DatabaseServiceCredentials serviceCredentials) {
        return createEntityManagerFactoryBean(createMysqlDataSource(serviceCredentials, "movies-mysql"), createHibernateJpaVendorAdapter(), MoviesBean.class);
    }

    @Bean
    public PlatformTransactionManager moviesPlatformTransactionManager(EntityManagerFactory moviesEntityManagerFactoryBean) {
        return new JpaTransactionManager(moviesEntityManagerFactoryBean);
    }

    @Bean
    public TransactionOperations moviesTransactionOperations(PlatformTransactionManager moviesPlatformTransactionManager) {
        return new TransactionTemplate(moviesPlatformTransactionManager);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsEntityManagerFactoryBean(DatabaseServiceCredentials serviceCredentials) {
        return createEntityManagerFactoryBean(createMysqlDataSource(serviceCredentials, "albums-mysql"), createHibernateJpaVendorAdapter(), AlbumsBean.class);
    }

    @Bean
    public PlatformTransactionManager albumsPlatformTransactionManager(EntityManagerFactory albumsEntityManagerFactoryBean) {
        return new JpaTransactionManager(albumsEntityManagerFactoryBean);
    }

    @Bean
    public TransactionOperations albumsTransactionOperations(PlatformTransactionManager albumsPlatformTransactionManager) {
        return new TransactionTemplate(albumsPlatformTransactionManager);
    }

    private static DataSource createMysqlDataSource(DatabaseServiceCredentials serviceCredentials, String name) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl(name));
        return dataSource;
    }

    private static LocalContainerEntityManagerFactoryBean createEntityManagerFactoryBean(DataSource dataSource, HibernateJpaVendorAdapter adapter, Class repositoryClass) {
        String packageName = repositoryClass.getPackage().getName(); // org.superbiz.moviefun.movies
        String unitName = repositoryClass.getSimpleName(); // MoviesBean
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setJpaVendorAdapter(adapter);
        factoryBean.setPackagesToScan(packageName);
        factoryBean.setPersistenceUnitName(unitName);
        return factoryBean;
    }

    private static HibernateJpaVendorAdapter createHibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adapter.setGenerateDdl(true);
        return adapter;
    }
}
