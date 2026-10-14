package com.acme.transport.safety.cameraevent.eventadjustment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCameraEventCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702268671)
        val items = mutableListOf<StrictCameraEventCycle>()
        for (index in 0 until 5) {
            items.add(newStrictCameraEventCycle("strict-camera-event-cycle-$index", "sample"))
        }
        val report = buildStrictCameraEventCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
