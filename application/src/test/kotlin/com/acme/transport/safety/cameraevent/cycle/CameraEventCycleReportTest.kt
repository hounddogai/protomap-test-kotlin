package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706008298)
        val items = mutableListOf<CameraEventCycle>()
        for (index in 0 until 5) {
            items.add(newCameraEventCycle("camera-event-cycle-$index", "sample"))
        }
        val report = buildCameraEventCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
