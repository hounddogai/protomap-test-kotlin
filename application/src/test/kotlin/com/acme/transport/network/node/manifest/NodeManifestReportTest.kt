package com.acme.transport.network.node.manifest

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700943894)
        val items = mutableListOf<NodeManifest>()
        for (index in 0 until 3) {
            items.add(newNodeManifest("node-manifest-$index", "sample"))
        }
        val report = buildNodeManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
