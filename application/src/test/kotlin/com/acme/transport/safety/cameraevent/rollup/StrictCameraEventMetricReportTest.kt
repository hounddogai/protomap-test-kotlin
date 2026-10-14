package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCameraEventMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709106440)
        val items = mutableListOf<StrictCameraEventMetric>()
        for (index in 0 until 5) {
            items.add(newStrictCameraEventMetric("strict-camera-event-metric-$index", "sample"))
        }
        val report = buildStrictCameraEventMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
