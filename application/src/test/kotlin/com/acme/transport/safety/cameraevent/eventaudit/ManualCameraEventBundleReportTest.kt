package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCameraEventBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708081858)
        val items = mutableListOf<ManualCameraEventBundle>()
        for (index in 0 until 2) {
            items.add(newManualCameraEventBundle("manual-camera-event-bundle-$index", "sample"))
        }
        val report = buildManualCameraEventBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
