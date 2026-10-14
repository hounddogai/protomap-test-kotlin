package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationBudgetStore()
        val item = newViolationBudget("code-67", "name-92")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
