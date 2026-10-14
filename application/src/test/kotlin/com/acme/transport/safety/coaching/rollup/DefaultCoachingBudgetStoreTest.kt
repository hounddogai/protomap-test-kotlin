package com.acme.transport.safety.coaching.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCoachingBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCoachingBudgetStore()
        val item = newDefaultCoachingBudget("code-833", "label-173")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
