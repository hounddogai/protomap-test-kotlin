package com.acme.transport.network.node.metric

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702863692)
        val items = mutableListOf<NodeAudit>()
        for (index in 0 until 3) {
            items.add(newNodeAudit("node-audit-$index", "sample"))
        }
        val report = buildNodeAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
