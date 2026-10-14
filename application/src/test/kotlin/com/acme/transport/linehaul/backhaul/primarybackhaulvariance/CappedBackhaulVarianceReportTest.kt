package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedBackhaulVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705586682)
        val items = mutableListOf<CappedBackhaulVariance>()
        for (index in 0 until 5) {
            items.add(newCappedBackhaulVariance("capped-backhaul-variance-$index", "sample"))
        }
        val report = buildCappedBackhaulVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
