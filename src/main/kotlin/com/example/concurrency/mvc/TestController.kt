package com.example.concurrency.mvc

import UseCase
import org.springframework.web.bind.annotation.*


@RestController
class TestController(
    private val useCase: UseCase,
    private val readService: ReadService,
    private val writeService: WriteService,
) {

    @GetMapping("/test")
    fun getAll() {
        readService.getAll()
    }

    @GetMapping("/test/{id}")
    fun get(@PathVariable id: Long) {
        readService.get(id)
    }

    @PostMapping("/test")
    fun test2() {
        writeService.create()
    }

    @PutMapping("/test/{id}")
    fun test(@PathVariable id: Long) {
        useCase.readAndWrite(id, "Update")
    }

    @DeleteMapping("/test/{id}")
    fun delete(@PathVariable id: Long) {
        writeService.delete(id)
    }

}