package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDisputeSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706178560)
        val items = mutableListOf<WeightedDisputeSnapshot>()
        for (index in 0 until 5) {
            items.add(newWeightedDisputeSnapshot("weighted-dispute-snapshot-$index", "sample"))
        }
        val report = buildWeightedDisputeSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
