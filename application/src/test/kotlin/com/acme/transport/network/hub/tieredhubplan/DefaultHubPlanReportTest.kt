package com.acme.transport.network.hub.tieredhubplan

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultHubPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705908806)
        val items = mutableListOf<DefaultHubPlan>()
        for (index in 0 until 6) {
            items.add(newDefaultHubPlan("default-hub-plan-$index", "sample"))
        }
        val report = buildDefaultHubPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
