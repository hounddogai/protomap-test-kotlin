package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingTenderBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingTenderBudgetStore()
        val item = newPendingTenderBudget("code-697", "title-871")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
