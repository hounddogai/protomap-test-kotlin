package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyViolationCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyViolationCycleStore()
        val item = newLegacyViolationCycle("code-17", "label-687")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
