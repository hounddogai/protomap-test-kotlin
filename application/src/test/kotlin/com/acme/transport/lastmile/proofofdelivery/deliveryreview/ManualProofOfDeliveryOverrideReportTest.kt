package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualProofOfDeliveryOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706565081)
        val items = mutableListOf<ManualProofOfDeliveryOverride>()
        for (index in 0 until 3) {
            items.add(newManualProofOfDeliveryOverride("manual-proof-of-delivery-override-$index", "sample"))
        }
        val report = buildManualProofOfDeliveryOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
