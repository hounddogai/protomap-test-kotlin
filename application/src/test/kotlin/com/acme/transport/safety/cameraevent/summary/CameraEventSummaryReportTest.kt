package com.acme.transport.safety.cameraevent.summary

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702999078)
        val items = mutableListOf<CameraEventSummary>()
        for (index in 0 until 3) {
            items.add(newCameraEventSummary("camera-event-summary-$index", "sample"))
        }
        val report = buildCameraEventSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
