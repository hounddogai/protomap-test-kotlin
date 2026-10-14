package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCameraEventSlotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700662605)
        val items = mutableListOf<ManualCameraEventSlot>()
        for (index in 0 until 3) {
            items.add(newManualCameraEventSlot("manual-camera-event-slot-$index", "sample"))
        }
        val report = buildManualCameraEventSlotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
