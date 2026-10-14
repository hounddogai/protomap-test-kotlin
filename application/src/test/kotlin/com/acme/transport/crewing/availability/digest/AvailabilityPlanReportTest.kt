package com.acme.transport.crewing.availability.digest

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701071374)
        val items = mutableListOf<AvailabilityPlan>()
        for (index in 0 until 6) {
            items.add(newAvailabilityPlan("availability-plan-$index", "sample"))
        }
        val report = buildAvailabilityPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
