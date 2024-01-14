package com.example.concurrency.mvc


import org.springframework.web.bind.annotation.*


@RestController
class TestController(
    private val readService: ReadService,
    private val writeService: WriteService,
    private val useCase: UseCase,

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

    @PutMapping("/test/{id}/throw")
    fun throwTest(@PathVariable id: Long) {
        useCase.transactionTest(id, "throw")
    }

    @DeleteMapping("/test/{id}")
    fun delete(@PathVariable id: Long) {
        writeService.delete(id)
    }

}