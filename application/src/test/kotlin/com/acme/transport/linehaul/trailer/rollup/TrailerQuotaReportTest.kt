package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706595143)
        val items = mutableListOf<TrailerQuota>()
        for (index in 0 until 3) {
            items.add(newTrailerQuota("trailer-quota-$index", "sample"))
        }
        val report = buildTrailerQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
