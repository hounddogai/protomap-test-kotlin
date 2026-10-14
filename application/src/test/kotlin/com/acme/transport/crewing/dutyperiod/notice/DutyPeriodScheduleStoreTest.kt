package com.acme.transport.crewing.dutyperiod.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodScheduleStore()
        val item = newDutyPeriodSchedule("code-932", "label-594")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
