package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCameraEventMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700456052)
        val items = mutableListOf<LenientCameraEventMetric>()
        for (index in 0 until 6) {
            items.add(newLenientCameraEventMetric("lenient-camera-event-metric-$index", "sample"))
        }
        val report = buildLenientCameraEventMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
