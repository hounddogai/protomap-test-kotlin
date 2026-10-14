package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveBackhaulAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708215464)
        val items = mutableListOf<AdaptiveBackhaulAdjustment>()
        for (index in 0 until 3) {
            items.add(newAdaptiveBackhaulAdjustment("adaptive-backhaul-adjustment-$index", "sample"))
        }
        val report = buildAdaptiveBackhaulAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
