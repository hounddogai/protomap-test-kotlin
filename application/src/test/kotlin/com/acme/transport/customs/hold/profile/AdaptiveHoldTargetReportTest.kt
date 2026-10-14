package com.acme.transport.customs.hold.profile

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveHoldTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709400342)
        val items = mutableListOf<AdaptiveHoldTarget>()
        for (index in 0 until 6) {
            items.add(newAdaptiveHoldTarget("adaptive-hold-target-$index", "sample"))
        }
        val report = buildAdaptiveHoldTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
