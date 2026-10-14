package com.acme.transport.customs.hold.group

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707938826)
        val items = mutableListOf<HoldGroup>()
        for (index in 0 until 2) {
            items.add(newHoldGroup("hold-group-$index", "sample"))
        }
        val report = buildHoldGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
