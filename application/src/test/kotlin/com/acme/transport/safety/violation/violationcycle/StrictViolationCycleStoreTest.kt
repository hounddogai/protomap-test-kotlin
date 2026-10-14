package com.acme.transport.safety.violation.violationcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictViolationCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictViolationCycleStore()
        val item = newStrictViolationCycle("code-298", "name-730")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
