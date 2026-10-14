package com.acme.transport.lastmile.parcel.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledParcelBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledParcelBudgetStore()
        val item = newScheduledParcelBudget("code-600", "title-203")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
