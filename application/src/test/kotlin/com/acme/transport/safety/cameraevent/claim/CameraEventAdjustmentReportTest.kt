package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705762683)
        val items = mutableListOf<CameraEventAdjustment>()
        for (index in 0 until 4) {
            items.add(newCameraEventAdjustment("camera-event-adjustment-$index", "sample"))
        }
        val report = buildCameraEventAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
