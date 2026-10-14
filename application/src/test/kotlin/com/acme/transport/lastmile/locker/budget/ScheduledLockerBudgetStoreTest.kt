package com.acme.transport.lastmile.locker.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledLockerBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledLockerBudgetStore()
        val item = newScheduledLockerBudget("code-345", "name-52")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
