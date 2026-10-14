package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDutyPeriodEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDutyPeriodEstimateStore()
        val item = newScheduledDutyPeriodEstimate("code-593", "title-768")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
