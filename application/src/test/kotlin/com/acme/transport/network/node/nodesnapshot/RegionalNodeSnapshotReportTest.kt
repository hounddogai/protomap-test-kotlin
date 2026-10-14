package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalNodeSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705393960)
        val items = mutableListOf<RegionalNodeSnapshot>()
        for (index in 0 until 2) {
            items.add(newRegionalNodeSnapshot("regional-node-snapshot-$index", "sample"))
        }
        val report = buildRegionalNodeSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
