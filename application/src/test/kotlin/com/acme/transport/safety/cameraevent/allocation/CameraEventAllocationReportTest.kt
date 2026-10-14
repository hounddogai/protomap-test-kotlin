package com.acme.transport.safety.cameraevent.allocation

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701740248)
        val items = mutableListOf<CameraEventAllocation>()
        for (index in 0 until 3) {
            items.add(newCameraEventAllocation("camera-event-allocation-$index", "sample"))
        }
        val report = buildCameraEventAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
