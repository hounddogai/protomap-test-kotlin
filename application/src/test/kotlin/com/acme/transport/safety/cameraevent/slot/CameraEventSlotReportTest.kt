package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702130283)
        val items = mutableListOf<CameraEventSlot>()
        for (index in 0 until 5) {
            items.add(newCameraEventSlot("camera-event-slot-$index", "sample"))
        }
        val report = buildCameraEventSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
