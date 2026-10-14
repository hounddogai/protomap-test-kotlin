package com.acme.transport.linehaul.trailer.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerBudgetStore()
        val item = newTrailerBudget("code-308", "label-387")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
