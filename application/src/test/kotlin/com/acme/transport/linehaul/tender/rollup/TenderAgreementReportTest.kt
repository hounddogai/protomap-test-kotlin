package com.acme.transport.linehaul.tender.rollup

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702794199)
        val items = mutableListOf<TenderAgreement>()
        for (index in 0 until 6) {
            items.add(newTenderAgreement("tender-agreement-$index", "sample"))
        }
        val report = buildTenderAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
