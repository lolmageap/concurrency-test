package com.example.concurrency

import jakarta.persistence.*

@Entity
@Table(name = "optimistic_entity")
class OptimisticEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    var likeCount: Long = 0,

    @Version
    var version: Long = 0,
)