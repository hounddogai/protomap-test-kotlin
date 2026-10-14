package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702008021)
        val items = mutableListOf<CameraEventEntry>()
        for (index in 0 until 2) {
            items.add(newCameraEventEntry("camera-event-entry-$index", "sample"))
        }
        val report = buildCameraEventEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
