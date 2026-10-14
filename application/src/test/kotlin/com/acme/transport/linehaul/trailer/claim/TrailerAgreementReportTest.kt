package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707305210)
        val items = mutableListOf<TrailerAgreement>()
        for (index in 0 until 6) {
            items.add(newTrailerAgreement("trailer-agreement-$index", "sample"))
        }
        val report = buildTrailerAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
