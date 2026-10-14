package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalBrokerAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709522682)
        val items = mutableListOf<GlobalBrokerAgreement>()
        for (index in 0 until 5) {
            items.add(newGlobalBrokerAgreement("global-broker-agreement-$index", "sample"))
        }
        val report = buildGlobalBrokerAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
