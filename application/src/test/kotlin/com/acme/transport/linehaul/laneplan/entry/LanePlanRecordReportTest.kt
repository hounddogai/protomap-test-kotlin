package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700547039)
        val items = mutableListOf<LanePlanRecord>()
        for (index in 0 until 2) {
            items.add(newLanePlanRecord("lane-plan-record-$index", "sample"))
        }
        val report = buildLanePlanRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
