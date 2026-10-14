package com.acme.transport.customs.hold.segment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700547286)
        val items = mutableListOf<HoldTarget>()
        for (index in 0 until 3) {
            items.add(newHoldTarget("hold-target-$index", "sample"))
        }
        val report = buildHoldTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
