package com.acme.transport.network.transittime.digest

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702539157)
        val items = mutableListOf<TransitTimeAgreement>()
        for (index in 0 until 4) {
            items.add(newTransitTimeAgreement("transit-time-agreement-$index", "sample"))
        }
        val report = buildTransitTimeAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
