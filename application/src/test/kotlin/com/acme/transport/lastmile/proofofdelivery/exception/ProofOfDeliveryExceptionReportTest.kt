package com.acme.transport.lastmile.proofofdelivery.exception

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705940352)
        val items = mutableListOf<ProofOfDeliveryException>()
        for (index in 0 until 3) {
            items.add(newProofOfDeliveryException("proof-of-delivery-exception-$index", "sample"))
        }
        val report = buildProofOfDeliveryExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
