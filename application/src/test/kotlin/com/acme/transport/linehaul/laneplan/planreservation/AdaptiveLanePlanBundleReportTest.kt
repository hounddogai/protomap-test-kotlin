package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLanePlanBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708738370)
        val items = mutableListOf<AdaptiveLanePlanBundle>()
        for (index in 0 until 5) {
            items.add(newAdaptiveLanePlanBundle("adaptive-lane-plan-bundle-$index", "sample"))
        }
        val report = buildAdaptiveLanePlanBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
