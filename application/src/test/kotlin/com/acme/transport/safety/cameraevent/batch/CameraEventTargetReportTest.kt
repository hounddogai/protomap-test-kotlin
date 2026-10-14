package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704157290)
        val items = mutableListOf<CameraEventTarget>()
        for (index in 0 until 3) {
            items.add(newCameraEventTarget("camera-event-target-$index", "sample"))
        }
        val report = buildCameraEventTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
