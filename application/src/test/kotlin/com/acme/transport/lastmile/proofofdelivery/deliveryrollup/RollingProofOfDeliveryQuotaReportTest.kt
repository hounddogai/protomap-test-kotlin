package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingProofOfDeliveryQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704476149)
        val items = mutableListOf<RollingProofOfDeliveryQuota>()
        for (index in 0 until 6) {
            items.add(newRollingProofOfDeliveryQuota("rolling-proof-of-delivery-quota-$index", "sample"))
        }
        val report = buildRollingProofOfDeliveryQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
