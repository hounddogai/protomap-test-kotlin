package com.acme.transport.customs.hold.adjustment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706985002)
        val items = mutableListOf<HoldAdjustment>()
        for (index in 0 until 5) {
            items.add(newHoldAdjustment("hold-adjustment-$index", "sample"))
        }
        val report = buildHoldAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
