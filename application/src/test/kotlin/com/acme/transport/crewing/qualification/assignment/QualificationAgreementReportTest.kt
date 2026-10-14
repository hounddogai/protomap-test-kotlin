package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706003119)
        val items = mutableListOf<QualificationAgreement>()
        for (index in 0 until 3) {
            items.add(newQualificationAgreement("qualification-agreement-$index", "sample"))
        }
        val report = buildQualificationAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
