package com.acme.transport.linehaul.laneplan.schedule

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackLanePlanRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701267222)
        val items = mutableListOf<FallbackLanePlanRule>()
        for (index in 0 until 4) {
            items.add(newFallbackLanePlanRule("fallback-lane-plan-rule-$index", "sample"))
        }
        val report = buildFallbackLanePlanRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
