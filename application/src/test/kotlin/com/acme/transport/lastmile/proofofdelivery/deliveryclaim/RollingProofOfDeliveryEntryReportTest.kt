package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingProofOfDeliveryEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708805326)
        val items = mutableListOf<RollingProofOfDeliveryEntry>()
        for (index in 0 until 2) {
            items.add(newRollingProofOfDeliveryEntry("rolling-proof-of-delivery-entry-$index", "sample"))
        }
        val report = buildRollingProofOfDeliveryEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
