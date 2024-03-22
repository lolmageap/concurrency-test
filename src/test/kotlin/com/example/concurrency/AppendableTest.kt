package com.example.concurrency

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

internal class AppendableTest : StringSpec({
    "appendable test" {
        val parents = Parents(
            name = "parent",
            age = 40,
            children = listOf(
                Children("child1", 10),
                Children("child2", 20),
                Children("child3", 30),
            )
        )

        val result = parents.children.joinToStringCustom(
            separator = ", ",
        ) {
            println(it)
            it.age
            it.name
        }
        result shouldBe "child1, child2, child3"

        val result2 = parents.children.joinToStringCustom(
            separator = ", ",
            prefix = "[",
            postfix = "]",
        ) { it.name }
        result2 shouldBe "[child1, child2, child3]"

        val result3 = parents.children.joinToStringCustom(
            separator = ", ",
            prefix = "[",
            postfix = "]",
            limit = 2,
            truncated = "...",
        ) { it.name }
        result3 shouldBe "[child1, child2, ...]"
    }
})

internal data class Parents(
    val name: String,
    val age: Int,
    val children: List<Children>
)

internal data class Children(
    val name: String,
    val age: Int,
)

internal fun <T> Iterable<T>.joinToStringCustom(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): String {
    return joinToCustom(
        StringBuilder(),
        separator,
        prefix,
        postfix,
        limit,
        truncated,
        transform,
    ).toString()
}

internal fun <T, A : Appendable> Iterable<T>.joinToCustom(
    buffer: A,
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): A {
    buffer.append(prefix)
    var count = 0
    for (element in this) {
        if (++count > 1) buffer.append(separator)

        // 0보다 작으면 limit 없이 배열의 size 만큼 실행, count 가 limit 보다 크면 break
        if (limit < 0 || count <= limit) {
            buffer.appendElement(element, transform)
        } else break
    }
    // limit 이 0보다 크고 count 가 limit 보다 크면 truncated 추가
    if (limit in 0 until count) buffer.append(truncated)
    buffer.append(postfix)
    return buffer
}

internal fun <T> Appendable.appendElement(element: T, transform: ((T) -> CharSequence)?) {
    when {
        transform != null -> append(
            transform(element)
        )
        element is CharSequence? -> append(element)
        element is Char -> append(element)
        else -> append(element.toString())
    }
}

/**
 * Appendable.appendElement:
 * transform 는 (T) -> CharSequence 타입의 함수를 argument 로 받음
 * T 타입의 인자를 받아 CharSequence 를 반환 하는 함수다.
 *
 * 위에서 작성된 T 타입은 Iterable<T>의 각 원소를 의미 한다.
 * element 는 Iterable<T>의 각 원소를 의미 하고 이 원소는 transform 함수의 인자로 전달 한다.
 *
 * transform 함수가 { it.name } 과 같이 정의 되었을 때 element 는 it의 역할을 하며
 * element.name 이 호출 되어 CharSequence 결과가 반환 되어 append 함수에 의해 buffer 에 추가
 *
 * 이 과정을 간단 하게 설명 하면:
 *
 * Iterable<T>의 각 원소(element)에 대해,
 * transform 함수를 element 에 적용 합니다. (transform(element))
 * transform 함수는 element 의 name 속성을 CharSequence 로 변환
 * 변환된 결과가 Appendable (buffer)에 추가
 * transform 은 element 를 사용 하여 어떤 연산을 수행 하고 그 결과를 반환 하는 함수 로서 동작
 */