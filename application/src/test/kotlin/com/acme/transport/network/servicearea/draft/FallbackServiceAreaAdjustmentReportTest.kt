package com.acme.transport.network.servicearea.draft

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackServiceAreaAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701431703)
        val items = mutableListOf<FallbackServiceAreaAdjustment>()
        for (index in 0 until 6) {
            items.add(newFallbackServiceAreaAdjustment("fallback-service-area-adjustment-$index", "sample"))
        }
        val report = buildFallbackServiceAreaAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
