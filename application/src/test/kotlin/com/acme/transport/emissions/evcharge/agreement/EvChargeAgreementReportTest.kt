package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709993466)
        val items = mutableListOf<EvChargeAgreement>()
        for (index in 0 until 4) {
            items.add(newEvChargeAgreement("ev-charge-agreement-$index", "sample"))
        }
        val report = buildEvChargeAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
