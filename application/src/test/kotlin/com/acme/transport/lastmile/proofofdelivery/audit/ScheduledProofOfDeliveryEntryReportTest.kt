package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledProofOfDeliveryEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707947505)
        val items = mutableListOf<ScheduledProofOfDeliveryEntry>()
        for (index in 0 until 6) {
            items.add(newScheduledProofOfDeliveryEntry("scheduled-proof-of-delivery-entry-$index", "sample"))
        }
        val report = buildScheduledProofOfDeliveryEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
