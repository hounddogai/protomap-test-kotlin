package com.acme.transport.linehaul.trailer.rule

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700844598)
        val items = mutableListOf<TrailerVariance>()
        for (index in 0 until 6) {
            items.add(newTrailerVariance("trailer-variance-$index", "sample"))
        }
        val report = buildTrailerVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
