package com.acme.transport.terminal.traileryard.yardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTrailerYardBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTrailerYardBudgetStore()
        val item = newBatchedTrailerYardBudget("code-198", "name-997")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
