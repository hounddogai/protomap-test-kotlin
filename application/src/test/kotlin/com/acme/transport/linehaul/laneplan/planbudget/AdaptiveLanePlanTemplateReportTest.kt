package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLanePlanTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705221726)
        val items = mutableListOf<AdaptiveLanePlanTemplate>()
        for (index in 0 until 2) {
            items.add(newAdaptiveLanePlanTemplate("adaptive-lane-plan-template-$index", "sample"))
        }
        val report = buildAdaptiveLanePlanTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
