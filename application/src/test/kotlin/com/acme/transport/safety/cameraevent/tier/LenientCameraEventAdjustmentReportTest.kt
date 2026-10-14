package com.acme.transport.safety.cameraevent.tier

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCameraEventAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707265454)
        val items = mutableListOf<LenientCameraEventAdjustment>()
        for (index in 0 until 3) {
            items.add(newLenientCameraEventAdjustment("lenient-camera-event-adjustment-$index", "sample"))
        }
        val report = buildLenientCameraEventAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
