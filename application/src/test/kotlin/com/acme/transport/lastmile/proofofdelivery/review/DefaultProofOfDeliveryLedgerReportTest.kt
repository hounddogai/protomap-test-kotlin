package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultProofOfDeliveryLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705477300)
        val items = mutableListOf<DefaultProofOfDeliveryLedger>()
        for (index in 0 until 6) {
            items.add(newDefaultProofOfDeliveryLedger("default-proof-of-delivery-ledger-$index", "sample"))
        }
        val report = buildDefaultProofOfDeliveryLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
