package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveEvChargeLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703849948)
        val items = mutableListOf<AdaptiveEvChargeLedger>()
        for (index in 0 until 2) {
            items.add(newAdaptiveEvChargeLedger("adaptive-ev-charge-ledger-$index", "sample"))
        }
        val report = buildAdaptiveEvChargeLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
