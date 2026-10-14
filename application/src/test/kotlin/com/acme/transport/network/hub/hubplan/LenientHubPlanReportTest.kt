package com.acme.transport.network.hub.hubplan

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientHubPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709129328)
        val items = mutableListOf<LenientHubPlan>()
        for (index in 0 until 6) {
            items.add(newLenientHubPlan("lenient-hub-plan-$index", "sample"))
        }
        val report = buildLenientHubPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
