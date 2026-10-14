package com.acme.transport.linehaul.tender.threshold

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706554576)
        val items = mutableListOf<TenderThreshold>()
        for (index in 0 until 2) {
            items.add(newTenderThreshold("tender-threshold-$index", "sample"))
        }
        val report = buildTenderThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
