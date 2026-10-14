package com.acme.transport.customs.clearance.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceBudgetStore()
        val item = newClearanceBudget("code-60", "name-662")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
