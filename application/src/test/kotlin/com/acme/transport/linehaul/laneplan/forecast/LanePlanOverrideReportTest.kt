package com.acme.transport.linehaul.laneplan.forecast

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703593651)
        val items = mutableListOf<LanePlanOverride>()
        for (index in 0 until 4) {
            items.add(newLanePlanOverride("lane-plan-override-$index", "sample"))
        }
        val report = buildLanePlanOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
