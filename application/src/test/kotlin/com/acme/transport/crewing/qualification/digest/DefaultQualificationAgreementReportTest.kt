package com.acme.transport.crewing.qualification.digest

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultQualificationAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709844437)
        val items = mutableListOf<DefaultQualificationAgreement>()
        for (index in 0 until 2) {
            items.add(newDefaultQualificationAgreement("default-qualification-agreement-$index", "sample"))
        }
        val report = buildDefaultQualificationAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
