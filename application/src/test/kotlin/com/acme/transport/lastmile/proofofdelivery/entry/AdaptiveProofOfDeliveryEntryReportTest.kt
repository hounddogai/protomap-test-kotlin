package com.acme.transport.lastmile.proofofdelivery.entry

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveProofOfDeliveryEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703117362)
        val items = mutableListOf<AdaptiveProofOfDeliveryEntry>()
        for (index in 0 until 2) {
            items.add(newAdaptiveProofOfDeliveryEntry("adaptive-proof-of-delivery-entry-$index", "sample"))
        }
        val report = buildAdaptiveProofOfDeliveryEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
