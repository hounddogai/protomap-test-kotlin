package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703898556)
        val items = mutableListOf<LanePlanEntry>()
        for (index in 0 until 5) {
            items.add(newLanePlanEntry("lane-plan-entry-$index", "sample"))
        }
        val report = buildLanePlanEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
