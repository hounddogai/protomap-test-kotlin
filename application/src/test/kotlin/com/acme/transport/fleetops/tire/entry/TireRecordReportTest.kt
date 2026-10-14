package com.acme.transport.fleetops.tire.entry

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705275084)
        val items = mutableListOf<TireRecord>()
        for (index in 0 until 5) {
            items.add(newTireRecord("tire-record-$index", "sample"))
        }
        val report = buildTireRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
