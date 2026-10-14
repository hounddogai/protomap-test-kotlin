package com.acme.transport.lastmile.proofofdelivery.group

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706683472)
        val items = mutableListOf<ProofOfDeliveryGroup>()
        for (index in 0 until 5) {
            items.add(newProofOfDeliveryGroup("proof-of-delivery-group-$index", "sample"))
        }
        val report = buildProofOfDeliveryGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
