package com.acme.transport.safety.nearmiss.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledNearMissBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledNearMissBudgetStore()
        val item = newScheduledNearMissBudget("code-812", "title-633")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
