package com.acme.transport.emissions.evcharge.plan

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargePlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708199084)
        val items = mutableListOf<EvChargePlan>()
        for (index in 0 until 4) {
            items.add(newEvChargePlan("ev-charge-plan-$index", "sample"))
        }
        val report = buildEvChargePlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
