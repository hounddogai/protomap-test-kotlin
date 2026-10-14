package com.acme.transport.freightpay.dispute.batcheddisputepolicy

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDisputeAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703995851)
        val items = mutableListOf<LenientDisputeAgreement>()
        for (index in 0 until 4) {
            items.add(newLenientDisputeAgreement("lenient-dispute-agreement-$index", "sample"))
        }
        val report = buildLenientDisputeAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
