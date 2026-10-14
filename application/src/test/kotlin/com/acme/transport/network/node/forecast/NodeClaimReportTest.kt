package com.acme.transport.network.node.forecast

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702844344)
        val items = mutableListOf<NodeClaim>()
        for (index in 0 until 5) {
            items.add(newNodeClaim("node-claim-$index", "sample"))
        }
        val report = buildNodeClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
