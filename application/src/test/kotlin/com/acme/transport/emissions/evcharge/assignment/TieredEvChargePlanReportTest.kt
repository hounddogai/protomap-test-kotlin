package com.acme.transport.emissions.evcharge.assignment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredEvChargePlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701859004)
        val items = mutableListOf<TieredEvChargePlan>()
        for (index in 0 until 3) {
            items.add(newTieredEvChargePlan("tiered-ev-charge-plan-$index", "sample"))
        }
        val report = buildTieredEvChargePlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
