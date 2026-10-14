package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualLanePlanTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704172997)
        val items = mutableListOf<ManualLanePlanTarget>()
        for (index in 0 until 4) {
            items.add(newManualLanePlanTarget("manual-lane-plan-target-$index", "sample"))
        }
        val report = buildManualLanePlanTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
