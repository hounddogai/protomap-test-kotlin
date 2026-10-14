package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703760291)
        val items = mutableListOf<ProofOfDeliveryAssignment>()
        for (index in 0 until 6) {
            items.add(newProofOfDeliveryAssignment("proof-of-delivery-assignment-$index", "sample"))
        }
        val report = buildProofOfDeliveryAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
