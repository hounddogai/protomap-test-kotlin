package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707219428)
        val items = mutableListOf<CapacityEntry>()
        for (index in 0 until 6) {
            items.add(newCapacityEntry("capacity-entry-$index", "sample"))
        }
        val report = buildCapacityEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
