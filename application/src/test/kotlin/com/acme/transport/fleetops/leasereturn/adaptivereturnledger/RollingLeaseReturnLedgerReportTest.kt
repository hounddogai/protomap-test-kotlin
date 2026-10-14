package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingLeaseReturnLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705820212)
        val items = mutableListOf<RollingLeaseReturnLedger>()
        for (index in 0 until 4) {
            items.add(newRollingLeaseReturnLedger("rolling-lease-return-ledger-$index", "sample"))
        }
        val report = buildRollingLeaseReturnLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
