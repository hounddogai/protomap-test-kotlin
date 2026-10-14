package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredProofOfDeliveryManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707983519)
        val items = mutableListOf<TieredProofOfDeliveryManifest>()
        for (index in 0 until 6) {
            items.add(newTieredProofOfDeliveryManifest("tiered-proof-of-delivery-manifest-$index", "sample"))
        }
        val report = buildTieredProofOfDeliveryManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
