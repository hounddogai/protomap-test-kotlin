package com.acme.transport.network.node.draft

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700293381)
        val items = mutableListOf<NodeScore>()
        for (index in 0 until 3) {
            items.add(newNodeScore("node-score-$index", "sample"))
        }
        val report = buildNodeScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
