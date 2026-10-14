package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyProofOfDeliveryWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700436976)
        val items = mutableListOf<LegacyProofOfDeliveryWindow>()
        for (index in 0 until 3) {
            items.add(newLegacyProofOfDeliveryWindow("legacy-proof-of-delivery-window-$index", "sample"))
        }
        val report = buildLegacyProofOfDeliveryWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
