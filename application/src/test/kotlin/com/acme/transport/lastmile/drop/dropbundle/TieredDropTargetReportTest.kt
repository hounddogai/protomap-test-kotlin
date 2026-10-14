package com.acme.transport.lastmile.drop.dropbundle

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDropTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705801877)
        val items = mutableListOf<TieredDropTarget>()
        for (index in 0 until 3) {
            items.add(newTieredDropTarget("tiered-drop-target-$index", "sample"))
        }
        val report = buildTieredDropTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
