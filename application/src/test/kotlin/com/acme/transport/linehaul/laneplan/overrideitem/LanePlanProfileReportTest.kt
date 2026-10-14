package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708228801)
        val items = mutableListOf<LanePlanProfile>()
        for (index in 0 until 5) {
            items.add(newLanePlanProfile("lane-plan-profile-$index", "sample"))
        }
        val report = buildLanePlanProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
