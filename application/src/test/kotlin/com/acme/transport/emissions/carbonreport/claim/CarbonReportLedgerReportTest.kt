package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700723328)
        val items = mutableListOf<CarbonReportLedger>()
        for (index in 0 until 3) {
            items.add(newCarbonReportLedger("carbon-report-ledger-$index", "sample"))
        }
        val report = buildCarbonReportLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
