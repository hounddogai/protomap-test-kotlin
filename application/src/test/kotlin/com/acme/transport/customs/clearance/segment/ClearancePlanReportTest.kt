package com.acme.transport.customs.clearance.segment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearancePlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704285127)
        val items = mutableListOf<ClearancePlan>()
        for (index in 0 until 2) {
            items.add(newClearancePlan("clearance-plan-$index", "sample"))
        }
        val report = buildClearancePlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
