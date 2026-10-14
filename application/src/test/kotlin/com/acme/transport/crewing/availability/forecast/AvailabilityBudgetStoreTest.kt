package com.acme.transport.crewing.availability.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityBudgetStore()
        val item = newAvailabilityBudget("code-563", "name-220")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
