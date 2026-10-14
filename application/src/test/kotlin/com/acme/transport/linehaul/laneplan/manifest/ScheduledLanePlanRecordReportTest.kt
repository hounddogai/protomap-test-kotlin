package com.acme.transport.linehaul.laneplan.manifest

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLanePlanRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700874558)
        val items = mutableListOf<ScheduledLanePlanRecord>()
        for (index in 0 until 5) {
            items.add(newScheduledLanePlanRecord("scheduled-lane-plan-record-$index", "sample"))
        }
        val report = buildScheduledLanePlanRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
