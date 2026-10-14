package com.acme.transport.crewing.dutyperiod.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDutyPeriodSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDutyPeriodSegmentStore()
        val item = newLenientDutyPeriodSegment("code-683", "label-362")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
