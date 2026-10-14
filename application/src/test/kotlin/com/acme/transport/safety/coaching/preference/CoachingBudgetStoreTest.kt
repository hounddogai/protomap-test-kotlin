package com.acme.transport.safety.coaching.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingBudgetStore()
        val item = newCoachingBudget("code-517", "label-415")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
