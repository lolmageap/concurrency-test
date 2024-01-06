package com.example.concurrency

import com.example.concurrency.Prefix.EMPTY
import io.kotest.core.test.TestCase

val TestCase.prefix: Prefix
    get() {
        val prefix = this.parent?.name?.prefix
            ?: return EMPTY

        return Prefix.convert(prefix)
    }

enum class Prefix {
    GIVEN, WHEN, THEN, EMPTY;
    companion object {
        fun convert(value: String) =
            when(value.lowercase().trim()) {
                "given:" -> GIVEN
                "when:" -> WHEN
                "then:" -> THEN
                else -> EMPTY
            }
    }
}
