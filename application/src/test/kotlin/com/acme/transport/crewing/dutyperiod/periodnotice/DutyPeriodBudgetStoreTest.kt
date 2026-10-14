package com.acme.transport.crewing.dutyperiod.periodnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodBudgetStore()
        val item = newDutyPeriodBudget("code-866", "label-19")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
