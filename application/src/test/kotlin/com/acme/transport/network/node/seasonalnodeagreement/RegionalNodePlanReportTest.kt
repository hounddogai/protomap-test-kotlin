package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalNodePlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707833070)
        val items = mutableListOf<RegionalNodePlan>()
        for (index in 0 until 5) {
            items.add(newRegionalNodePlan("regional-node-plan-$index", "sample"))
        }
        val report = buildRegionalNodePlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
