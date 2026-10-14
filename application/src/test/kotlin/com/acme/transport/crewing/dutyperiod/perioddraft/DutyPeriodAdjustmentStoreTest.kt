package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodAdjustmentStore()
        val item = newDutyPeriodAdjustment("code-483", "label-184")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
