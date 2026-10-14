package com.acme.transport.safety.violation.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalViolationBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalViolationBudgetStore()
        val item = newGlobalViolationBudget("code-662", "label-469")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
