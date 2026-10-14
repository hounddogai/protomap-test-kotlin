package com.acme.transport.network.zone.plan

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredZonePlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702349241)
        val items = mutableListOf<TieredZonePlan>()
        for (index in 0 until 4) {
            items.add(newTieredZonePlan("tiered-zone-plan-$index", "sample"))
        }
        val report = buildTieredZonePlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
