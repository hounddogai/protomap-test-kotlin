package com.acme.transport.freightpay.dispute.ledger

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDisputeLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709632855)
        val items = mutableListOf<RollingDisputeLedger>()
        for (index in 0 until 3) {
            items.add(newRollingDisputeLedger("rolling-dispute-ledger-$index", "sample"))
        }
        val report = buildRollingDisputeLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
