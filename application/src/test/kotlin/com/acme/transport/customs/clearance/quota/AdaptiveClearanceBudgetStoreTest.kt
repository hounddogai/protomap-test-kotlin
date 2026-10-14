package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveClearanceBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveClearanceBudgetStore()
        val item = newAdaptiveClearanceBudget("code-141", "label-123")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
