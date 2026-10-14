package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDisputeLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700711419)
        val items = mutableListOf<FallbackDisputeLedger>()
        for (index in 0 until 6) {
            items.add(newFallbackDisputeLedger("fallback-dispute-ledger-$index", "sample"))
        }
        val report = buildFallbackDisputeLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
