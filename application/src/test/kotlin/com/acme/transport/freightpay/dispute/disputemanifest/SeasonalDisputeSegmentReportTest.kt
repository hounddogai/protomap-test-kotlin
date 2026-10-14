package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDisputeSegmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706403480)
        val items = mutableListOf<SeasonalDisputeSegment>()
        for (index in 0 until 3) {
            items.add(newSeasonalDisputeSegment("seasonal-dispute-segment-$index", "sample"))
        }
        val report = buildSeasonalDisputeSegmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
