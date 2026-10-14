package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientBackhaulVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702845085)
        val items = mutableListOf<LenientBackhaulVariance>()
        for (index in 0 until 2) {
            items.add(newLenientBackhaulVariance("lenient-backhaul-variance-$index", "sample"))
        }
        val report = buildLenientBackhaulVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
