package com.acme.transport.safety.violation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientViolationExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientViolationExceptionStore()
        val item = newLenientViolationException("code-613", "title-626")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
