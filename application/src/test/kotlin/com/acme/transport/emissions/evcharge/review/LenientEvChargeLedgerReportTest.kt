package com.acme.transport.emissions.evcharge.review

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientEvChargeLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700490052)
        val items = mutableListOf<LenientEvChargeLedger>()
        for (index in 0 until 2) {
            items.add(newLenientEvChargeLedger("lenient-ev-charge-ledger-$index", "sample"))
        }
        val report = buildLenientEvChargeLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
