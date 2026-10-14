package com.acme.transport.crewing.qualification.digest

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedQualificationAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702706830)
        val items = mutableListOf<CappedQualificationAgreement>()
        for (index in 0 until 6) {
            items.add(newCappedQualificationAgreement("capped-qualification-agreement-$index", "sample"))
        }
        val report = buildCappedQualificationAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
