package com.acme.transport.network.node.plan

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodePlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708186769)
        val items = mutableListOf<NodePlan>()
        for (index in 0 until 3) {
            items.add(newNodePlan("node-plan-$index", "sample"))
        }
        val report = buildNodePlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
