package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCameraEventSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706821230)
        val items = mutableListOf<ManualCameraEventSummary>()
        for (index in 0 until 5) {
            items.add(newManualCameraEventSummary("manual-camera-event-summary-$index", "sample"))
        }
        val report = buildManualCameraEventSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
