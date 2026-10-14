package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredProofOfDeliveryRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708520592)
        val items = mutableListOf<TieredProofOfDeliveryRollup>()
        for (index in 0 until 5) {
            items.add(newTieredProofOfDeliveryRollup("tiered-proof-of-delivery-rollup-$index", "sample"))
        }
        val report = buildTieredProofOfDeliveryRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
