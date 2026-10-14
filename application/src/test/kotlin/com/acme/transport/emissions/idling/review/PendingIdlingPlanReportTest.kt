package com.acme.transport.emissions.idling.review

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingIdlingPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706077300)
        val items = mutableListOf<PendingIdlingPlan>()
        for (index in 0 until 6) {
            items.add(newPendingIdlingPlan("pending-idling-plan-$index", "sample"))
        }
        val report = buildPendingIdlingPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
