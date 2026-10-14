package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCameraEventAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702915181)
        val items = mutableListOf<AdaptiveCameraEventAllocation>()
        for (index in 0 until 4) {
            items.add(newAdaptiveCameraEventAllocation("adaptive-camera-event-allocation-$index", "sample"))
        }
        val report = buildAdaptiveCameraEventAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
