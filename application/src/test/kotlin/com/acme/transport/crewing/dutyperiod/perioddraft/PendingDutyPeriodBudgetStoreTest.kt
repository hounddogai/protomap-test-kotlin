package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDutyPeriodBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDutyPeriodBudgetStore()
        val item = newPendingDutyPeriodBudget("code-871", "name-163")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
