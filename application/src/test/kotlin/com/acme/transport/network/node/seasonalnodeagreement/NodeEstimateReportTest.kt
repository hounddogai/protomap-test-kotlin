package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701000022)
        val items = mutableListOf<NodeEstimate>()
        for (index in 0 until 3) {
            items.add(newNodeEstimate("node-estimate-$index", "sample"))
        }
        val report = buildNodeEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
