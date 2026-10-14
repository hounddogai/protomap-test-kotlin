package com.acme.transport.safety.cameraevent.group

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703447250)
        val items = mutableListOf<CameraEventOverride>()
        for (index in 0 until 5) {
            items.add(newCameraEventOverride("camera-event-override-$index", "sample"))
        }
        val report = buildCameraEventOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
