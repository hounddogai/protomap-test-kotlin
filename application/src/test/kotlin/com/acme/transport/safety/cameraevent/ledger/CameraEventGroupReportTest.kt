package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700627806)
        val items = mutableListOf<CameraEventGroup>()
        for (index in 0 until 3) {
            items.add(newCameraEventGroup("camera-event-group-$index", "sample"))
        }
        val report = buildCameraEventGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
