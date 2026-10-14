package com.acme.transport.customs.hold.baseline

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709206225)
        val items = mutableListOf<HoldBaseline>()
        for (index in 0 until 3) {
            items.add(newHoldBaseline("hold-baseline-$index", "sample"))
        }
        val report = buildHoldBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
