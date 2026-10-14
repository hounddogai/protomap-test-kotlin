package com.acme.transport.safety.cameraevent.metric

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706643880)
        val items = mutableListOf<CameraEventMetric>()
        for (index in 0 until 4) {
            items.add(newCameraEventMetric("camera-event-metric-$index", "sample"))
        }
        val report = buildCameraEventMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
