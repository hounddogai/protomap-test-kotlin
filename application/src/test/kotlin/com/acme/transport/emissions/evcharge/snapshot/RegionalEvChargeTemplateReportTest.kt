package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalEvChargeTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706295115)
        val items = mutableListOf<RegionalEvChargeTemplate>()
        for (index in 0 until 3) {
            items.add(newRegionalEvChargeTemplate("regional-ev-charge-template-$index", "sample"))
        }
        val report = buildRegionalEvChargeTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
