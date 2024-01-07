package com.example.concurrency.rdbms

object DatabaseProperty {
    const val MASTER_DATASOURCE = "masterDataSource"
    const val SLAVE_DATASOURCE = "slaveDataSource"
    const val SLAVE = "slave"
    const val MASTER = "master"
    const val MASTER_PATH = "spring.datasource.master.hikari"
    const val SLAVE_PATH = "spring.datasource.slave.hikari"
}