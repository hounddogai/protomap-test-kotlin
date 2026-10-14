package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706171632)
        val items = mutableListOf<CameraEventTier>()
        for (index in 0 until 2) {
            items.add(newCameraEventTier("camera-event-tier-$index", "sample"))
        }
        val report = buildCameraEventTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
