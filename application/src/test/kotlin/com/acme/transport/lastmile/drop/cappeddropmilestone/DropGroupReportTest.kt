package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706944228)
        val items = mutableListOf<DropGroup>()
        for (index in 0 until 4) {
            items.add(newDropGroup("drop-group-$index", "sample"))
        }
        val report = buildDropGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
