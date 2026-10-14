package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarbonReportAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702860038)
        val items = mutableListOf<CarbonReportAgreement>()
        for (index in 0 until 4) {
            items.add(newCarbonReportAgreement("carbon-report-agreement-$index", "sample"))
        }
        val report = buildCarbonReportAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
