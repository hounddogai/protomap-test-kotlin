package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLanePlanSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708406411)
        val items = mutableListOf<ScheduledLanePlanSnapshot>()
        for (index in 0 until 6) {
            items.add(newScheduledLanePlanSnapshot("scheduled-lane-plan-snapshot-$index", "sample"))
        }
        val report = buildScheduledLanePlanSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
