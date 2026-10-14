package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDutyPeriodScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDutyPeriodScheduleStore()
        val item = newSeasonalDutyPeriodSchedule("code-997", "name-369")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
