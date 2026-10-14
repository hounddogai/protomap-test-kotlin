package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedInspectionBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedInspectionBudgetStore()
        val item = newBatchedInspectionBudget("code-48", "title-471")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
