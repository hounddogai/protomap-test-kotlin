package com.acme.transport.emissions.evcharge.chargequota

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredEvChargeAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705291564)
        val items = mutableListOf<TieredEvChargeAgreement>()
        for (index in 0 until 2) {
            items.add(newTieredEvChargeAgreement("tiered-ev-charge-agreement-$index", "sample"))
        }
        val report = buildTieredEvChargeAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
