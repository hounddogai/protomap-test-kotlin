package com.acme.transport.linehaul.tender.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderBudgetStore()
        val item = newTenderBudget("code-325", "label-428")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
