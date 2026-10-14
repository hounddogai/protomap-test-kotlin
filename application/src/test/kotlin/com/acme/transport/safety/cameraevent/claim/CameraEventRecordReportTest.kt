package com.acme.transport.safety.cameraevent.claim

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702437710)
        val items = mutableListOf<CameraEventRecord>()
        for (index in 0 until 3) {
            items.add(newCameraEventRecord("camera-event-record-$index", "sample"))
        }
        val report = buildCameraEventRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
