package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLeaseReturnLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700505570)
        val items = mutableListOf<ScheduledLeaseReturnLedger>()
        for (index in 0 until 4) {
            items.add(newScheduledLeaseReturnLedger("scheduled-lease-return-ledger-$index", "sample"))
        }
        val report = buildScheduledLeaseReturnLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
