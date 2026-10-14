package com.acme.transport.crewing.dutyperiod.periodnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveDutyPeriodSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveDutyPeriodSegmentStore()
        val item = newAdaptiveDutyPeriodSegment("code-24", "name-362")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
