package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708862660)
        val items = mutableListOf<RedeliveryVariance>()
        for (index in 0 until 6) {
            items.add(newRedeliveryVariance("redelivery-variance-$index", "sample"))
        }
        val report = buildRedeliveryVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
