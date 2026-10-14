package com.acme.transport.crewing.dutyperiod.periodassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodCycleStore()
        val item = newDutyPeriodCycle("code-956", "title-9")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
