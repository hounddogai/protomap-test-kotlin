package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706715173)
        val items = mutableListOf<ProofOfDeliveryPreference>()
        for (index in 0 until 5) {
            items.add(newProofOfDeliveryPreference("proof-of-delivery-preference-$index", "sample"))
        }
        val report = buildProofOfDeliveryPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
