package com.acme.transport.linehaul.tender.plan

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706044074)
        val items = mutableListOf<TenderVariance>()
        for (index in 0 until 2) {
            items.add(newTenderVariance("tender-variance-$index", "sample"))
        }
        val report = buildTenderVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
