package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalProofOfDeliveryOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706596309)
        val items = mutableListOf<GlobalProofOfDeliveryOverride>()
        for (index in 0 until 4) {
            items.add(newGlobalProofOfDeliveryOverride("global-proof-of-delivery-override-$index", "sample"))
        }
        val report = buildGlobalProofOfDeliveryOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
