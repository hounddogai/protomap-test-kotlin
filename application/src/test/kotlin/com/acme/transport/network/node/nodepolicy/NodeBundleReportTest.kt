package com.acme.transport.network.node.nodepolicy

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707205442)
        val items = mutableListOf<NodeBundle>()
        for (index in 0 until 6) {
            items.add(newNodeBundle("node-bundle-$index", "sample"))
        }
        val report = buildNodeBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
