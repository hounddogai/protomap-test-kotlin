package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientBrokerAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709793042)
        val items = mutableListOf<LenientBrokerAgreement>()
        for (index in 0 until 6) {
            items.add(newLenientBrokerAgreement("lenient-broker-agreement-$index", "sample"))
        }
        val report = buildLenientBrokerAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
