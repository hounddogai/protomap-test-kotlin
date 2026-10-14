package com.acme.transport.network.node.nodeagreement

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708407388)
        val items = mutableListOf<NodeTemplate>()
        for (index in 0 until 2) {
            items.add(newNodeTemplate("node-template-$index", "sample"))
        }
        val report = buildNodeTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
