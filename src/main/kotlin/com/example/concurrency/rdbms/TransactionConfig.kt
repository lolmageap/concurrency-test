package com.example.concurrency.rdbms

import com.example.concurrency.rdbms.DatabaseProperty.MASTER
import com.example.concurrency.rdbms.DatabaseProperty.MASTER_DATASOURCE
import com.example.concurrency.rdbms.DatabaseProperty.MASTER_PATH
import com.example.concurrency.rdbms.DatabaseProperty.SLAVE
import com.example.concurrency.rdbms.DatabaseProperty.SLAVE_DATASOURCE
import com.example.concurrency.rdbms.DatabaseProperty.SLAVE_PATH
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.sql.DataSource

@Configuration
class TransactionConfig {

    @Bean(name = [MASTER_DATASOURCE])
    @ConfigurationProperties(MASTER_PATH)
    fun masterDataSource(): HikariDataSource =
        DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()

    @Bean(name = [SLAVE_DATASOURCE])
    @ConfigurationProperties(SLAVE_PATH)
    fun slaveDataSource(): HikariDataSource =
        DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()
            .apply { this.isReadOnly = true }

    @Bean
    @DependsOn(MASTER_DATASOURCE, SLAVE_DATASOURCE)
    fun routingDataSource(
        @Qualifier(MASTER_DATASOURCE) masterDataSource: DataSource,
        @Qualifier(SLAVE_DATASOURCE) slaveDataSource: DataSource,
    ): DataSource {
        val dataSources = hashMapOf<Any, Any>().apply {
            this[MASTER] = masterDataSource
            this[SLAVE] = slaveDataSource
        }
        return RoutingDataSource().apply {
            setTargetDataSources(dataSources)
            setDefaultTargetDataSource(masterDataSource)
        }
    }

    @Bean
    @Primary
    @DependsOn("routingDataSource")
    fun dataSource(routingDataSource: DataSource) =
        LazyConnectionDataSourceProxy(routingDataSource)
}

class RoutingDataSource : AbstractRoutingDataSource() {
    override fun determineCurrentLookupKey(): Any =
        when {
            TransactionSynchronizationManager.isCurrentTransactionReadOnly() -> SLAVE
            else -> MASTER
        }.also {
            println("Current lookup key: $it")
        }
}
