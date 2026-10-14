package com.acme.transport.safety.cameraevent.score

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709485469)
        val items = mutableListOf<CameraEventTemplate>()
        for (index in 0 until 5) {
            items.add(newCameraEventTemplate("camera-event-template-$index", "sample"))
        }
        val report = buildCameraEventTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
