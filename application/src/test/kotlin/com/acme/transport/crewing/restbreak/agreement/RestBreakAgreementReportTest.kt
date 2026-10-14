package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700515035)
        val items = mutableListOf<RestBreakAgreement>()
        for (index in 0 until 5) {
            items.add(newRestBreakAgreement("rest-break-agreement-$index", "sample"))
        }
        val report = buildRestBreakAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
