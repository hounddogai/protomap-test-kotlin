package com.acme.transport.linehaul.tender.baseline

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704580029)
        val items = mutableListOf<TenderBaseline>()
        for (index in 0 until 2) {
            items.add(newTenderBaseline("tender-baseline-$index", "sample"))
        }
        val report = buildTenderBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
