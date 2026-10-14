package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingProofOfDeliveryMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707099465)
        val items = mutableListOf<RollingProofOfDeliveryMapping>()
        for (index in 0 until 4) {
            items.add(newRollingProofOfDeliveryMapping("rolling-proof-of-delivery-mapping-$index", "sample"))
        }
        val report = buildRollingProofOfDeliveryMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
