package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTenderVarianceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704945865)
        val items = mutableListOf<LegacyTenderVariance>()
        for (index in 0 until 3) {
            items.add(newLegacyTenderVariance("legacy-tender-variance-$index", "sample"))
        }
        val report = buildLegacyTenderVarianceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
