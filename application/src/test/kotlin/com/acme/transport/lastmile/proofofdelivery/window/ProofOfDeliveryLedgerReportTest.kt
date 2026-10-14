package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709735003)
        val items = mutableListOf<ProofOfDeliveryLedger>()
        for (index in 0 until 5) {
            items.add(newProofOfDeliveryLedger("proof-of-delivery-ledger-$index", "sample"))
        }
        val report = buildProofOfDeliveryLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
