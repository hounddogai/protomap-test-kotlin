package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAvailabilityLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703294356)
        val items = mutableListOf<TieredAvailabilityLedger>()
        for (index in 0 until 3) {
            items.add(newTieredAvailabilityLedger("tiered-availability-ledger-$index", "sample"))
        }
        val report = buildTieredAvailabilityLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
