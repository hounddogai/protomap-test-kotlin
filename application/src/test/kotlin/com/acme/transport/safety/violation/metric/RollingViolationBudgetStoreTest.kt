package com.acme.transport.safety.violation.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingViolationBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingViolationBudgetStore()
        val item = newRollingViolationBudget("code-780", "title-20")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
