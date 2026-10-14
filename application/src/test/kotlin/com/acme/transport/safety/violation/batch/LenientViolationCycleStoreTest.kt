package com.acme.transport.safety.violation.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientViolationCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientViolationCycleStore()
        val item = newLenientViolationCycle("code-321", "label-977")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
