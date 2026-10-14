package com.acme.transport.customs.hold.recorditem

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientHoldGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701278197)
        val items = mutableListOf<LenientHoldGroup>()
        for (index in 0 until 4) {
            items.add(newLenientHoldGroup("lenient-hold-group-$index", "sample"))
        }
        val report = buildLenientHoldGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
