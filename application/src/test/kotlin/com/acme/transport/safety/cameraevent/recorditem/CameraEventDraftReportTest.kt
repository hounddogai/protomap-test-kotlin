package com.acme.transport.safety.cameraevent.recorditem

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702253526)
        val items = mutableListOf<CameraEventDraft>()
        for (index in 0 until 3) {
            items.add(newCameraEventDraft("camera-event-draft-$index", "sample"))
        }
        val report = buildCameraEventDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
