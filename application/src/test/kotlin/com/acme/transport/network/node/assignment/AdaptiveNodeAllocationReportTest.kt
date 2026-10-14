package com.acme.transport.network.node.assignment

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveNodeAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708964197)
        val items = mutableListOf<AdaptiveNodeAllocation>()
        for (index in 0 until 6) {
            items.add(newAdaptiveNodeAllocation("adaptive-node-allocation-$index", "sample"))
        }
        val report = buildAdaptiveNodeAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
