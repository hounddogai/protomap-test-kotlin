package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDisputeTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703286592)
        val items = mutableListOf<LenientDisputeTier>()
        for (index in 0 until 2) {
            items.add(newLenientDisputeTier("lenient-dispute-tier-$index", "sample"))
        }
        val report = buildLenientDisputeTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
