package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702397395)
        val items = mutableListOf<RestBreakAdjustment>()
        for (index in 0 until 2) {
            items.add(newRestBreakAdjustment("rest-break-adjustment-$index", "sample"))
        }
        val report = buildRestBreakAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
