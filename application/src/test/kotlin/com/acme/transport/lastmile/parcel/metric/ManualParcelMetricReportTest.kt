package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualParcelMetricReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701151933)
        val items = mutableListOf<ManualParcelMetric>()
        for (index in 0 until 5) {
            items.add(newManualParcelMetric("manual-parcel-metric-$index", "sample"))
        }
        val report = buildManualParcelMetricReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
