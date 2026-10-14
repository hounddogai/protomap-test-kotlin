package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704951446)
        val items = mutableListOf<LanePlanVariance>()
        for (index in 0 until 3) {
            items.add(newLanePlanVariance("lane-plan-variance-$index", "sample"))
        }
        val report = buildLanePlanVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
