package com.acme.transport.safety.cameraevent.tier

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCameraEventTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700006373)
        val items = mutableListOf<StrictCameraEventTier>()
        for (index in 0 until 2) {
            items.add(newStrictCameraEventTier("strict-camera-event-tier-$index", "sample"))
        }
        val report = buildStrictCameraEventTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
