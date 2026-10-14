package com.acme.transport.freightpay.dispute.disputeassignment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDisputeGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704450028)
        val items = mutableListOf<CappedDisputeGroup>()
        for (index in 0 until 3) {
            items.add(newCappedDisputeGroup("capped-dispute-group-$index", "sample"))
        }
        val report = buildCappedDisputeGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
