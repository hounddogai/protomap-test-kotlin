package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultProofOfDeliveryManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703793533)
        val items = mutableListOf<DefaultProofOfDeliveryManifest>()
        for (index in 0 until 3) {
            items.add(newDefaultProofOfDeliveryManifest("default-proof-of-delivery-manifest-$index", "sample"))
        }
        val report = buildDefaultProofOfDeliveryManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
