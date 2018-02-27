package ${package}.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Component
public class InitDataSource implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private Logger logger = Logger.getLogger(InitDataSource.class);

    private int count = 0;

    private List<Database> databases = new ArrayList<>();

    private static final String prefix = "Mybatis.Database[%d].%s";

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {

        for (Database database : databases) {
            this.initDatabase(database, beanDefinitionRegistry);
        }
    }

    private void initDatabase(Database database, BeanDefinitionRegistry beanDefinitionRegistry) {
        this.initDataSource(database, beanDefinitionRegistry);
        this.initSqlSessionFactory(database, beanDefinitionRegistry);
        this.initMapperScanner(database, beanDefinitionRegistry);
        this.initTransactionManager(database, beanDefinitionRegistry);
    }

    //数据源初始化
    private void initDataSource(Database database, BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinitionBuilder dataSourceDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(HikariDataSource.class);
        dataSourceDefinitionBuilder.addPropertyValue("driverClassName", database.getDriverClassName());
        dataSourceDefinitionBuilder.addPropertyValue("jdbcUrl", database.getJdbcUrl());
        dataSourceDefinitionBuilder.addPropertyValue("username", database.getUsername());
        dataSourceDefinitionBuilder.addPropertyValue("password", database.getPassword());
        dataSourceDefinitionBuilder.addPropertyValue("readOnly", false);
        dataSourceDefinitionBuilder.addPropertyValue("connectionTestQuery", database.getConnectionTestQuery());
        dataSourceDefinitionBuilder.addPropertyValue("connectionTimeout", database.getConnectionTimeout());
        dataSourceDefinitionBuilder.addPropertyValue("validationTimeout", database.getValidationTimeout());
        dataSourceDefinitionBuilder.addPropertyValue("idleTimeout", database.getIdleTimeout());
        dataSourceDefinitionBuilder.addPropertyValue("maxLifetime", database.getMaxLifetime());
        dataSourceDefinitionBuilder.addPropertyValue("maximumPoolSize", database.getMaximumPoolSize());
        dataSourceDefinitionBuilder.addPropertyValue("minimumIdle", database.getMinimumIdle());
        dataSourceDefinitionBuilder.setDestroyMethodName("close");
        beanDefinitionRegistry.registerBeanDefinition(database.getDatasource(), dataSourceDefinitionBuilder.getRawBeanDefinition());
    }

    //工厂初始化
    private void initSqlSessionFactory(Database database, BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinitionBuilder sqlSessionFactoryBean = BeanDefinitionBuilder.genericBeanDefinition(SqlSessionFactoryBean.class);
        sqlSessionFactoryBean.addPropertyValue("mapperLocations", database.getMapperLocations());
        sqlSessionFactoryBean.addPropertyValue("typeAliasesPackage", database.getTypeAliasesPackage());
        sqlSessionFactoryBean.addPropertyReference("dataSource", database.getDatasource());
        beanDefinitionRegistry.registerBeanDefinition(database.getSqlSessionFactory(), sqlSessionFactoryBean.getRawBeanDefinition());
    }

    //事务管理器初始化
    private void initTransactionManager(Database database, BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinitionBuilder dataSourceTransactionManager = BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class);
        dataSourceTransactionManager.addPropertyReference("dataSource", database.getDatasource());
        beanDefinitionRegistry.registerBeanDefinition(database.getTransactionManager(), dataSourceTransactionManager.getRawBeanDefinition());
    }

    private void initMapperScanner(Database database, BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinitionBuilder mapperScanner = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
        mapperScanner.addPropertyValue("basePackage", database.getMapper());
        mapperScanner.addPropertyReference("sqlSessionFactory", database.getSqlSessionFactory());
        beanDefinitionRegistry.registerBeanDefinition(database.getMapper(), mapperScanner.getBeanDefinition());
    }

    //配置载入
    @Override
    public void setEnvironment(Environment environment) {
        ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
        Field[] dataBaseField = Database.class.getDeclaredFields();
        Field fieldTemp = dataBaseField[0];
        this.count = 0;

        while (!StringUtils.isEmpty(env.getProperty(String.format(prefix, count, fieldTemp.getName())))) {
            Database database = new Database();

            for (Field field : dataBaseField) {
                field.setAccessible(true);
                try {
                    field.set(database, env.getProperty(String.format(prefix, count, field.getName()), field.getType()));
                } catch (IllegalAccessException e) {
                    logger.error("配置数据读取异常" + e.getCause().getMessage());
                }
            }
            this.databases.add(database);
            count++;
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
