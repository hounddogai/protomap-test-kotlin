package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalLeaseReturnLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709757889)
        val items = mutableListOf<SeasonalLeaseReturnLedger>()
        for (index in 0 until 2) {
            items.add(newSeasonalLeaseReturnLedger("seasonal-lease-return-ledger-$index", "sample"))
        }
        val report = buildSeasonalLeaseReturnLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
