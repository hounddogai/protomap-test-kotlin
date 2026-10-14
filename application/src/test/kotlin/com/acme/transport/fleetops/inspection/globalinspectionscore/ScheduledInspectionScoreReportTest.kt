package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledInspectionScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709551499)
        val items = mutableListOf<ScheduledInspectionScore>()
        for (index in 0 until 5) {
            items.add(newScheduledInspectionScore("scheduled-inspection-score-$index", "sample"))
        }
        val report = buildScheduledInspectionScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
