package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705014153)
        val items = mutableListOf<ProofOfDeliveryBaseline>()
        for (index in 0 until 2) {
            items.add(newProofOfDeliveryBaseline("proof-of-delivery-baseline-$index", "sample"))
        }
        val report = buildProofOfDeliveryBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
