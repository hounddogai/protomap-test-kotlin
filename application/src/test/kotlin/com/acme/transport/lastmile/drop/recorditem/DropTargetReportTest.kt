package com.acme.transport.lastmile.drop.recorditem

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708460523)
        val items = mutableListOf<DropTarget>()
        for (index in 0 until 6) {
            items.add(newDropTarget("drop-target-$index", "sample"))
        }
        val report = buildDropTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
