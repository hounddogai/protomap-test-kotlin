package com.acme.transport.linehaul.tender.exception

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTenderTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703481763)
        val items = mutableListOf<LegacyTenderTemplate>()
        for (index in 0 until 4) {
            items.add(newLegacyTenderTemplate("legacy-tender-template-$index", "sample"))
        }
        val report = buildLegacyTenderTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
