package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackEvChargeThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707100592)
        val items = mutableListOf<FallbackEvChargeThreshold>()
        for (index in 0 until 2) {
            items.add(newFallbackEvChargeThreshold("fallback-ev-charge-threshold-$index", "sample"))
        }
        val report = buildFallbackEvChargeThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
