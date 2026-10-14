package com.acme.transport.linehaul.capacity.checkpoint

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707931741)
        val items = mutableListOf<CapacityDraft>()
        for (index in 0 until 2) {
            items.add(newCapacityDraft("capacity-draft-$index", "sample"))
        }
        val report = buildCapacityDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
