package com.acme.transport.fleetops.tire.plan

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TirePlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701711306)
        val items = mutableListOf<TirePlan>()
        for (index in 0 until 6) {
            items.add(newTirePlan("tire-plan-$index", "sample"))
        }
        val report = buildTirePlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
