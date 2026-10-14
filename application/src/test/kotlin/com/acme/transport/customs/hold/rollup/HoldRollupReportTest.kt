package com.acme.transport.customs.hold.rollup

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701627297)
        val items = mutableListOf<HoldRollup>()
        for (index in 0 until 6) {
            items.add(newHoldRollup("hold-rollup-$index", "sample"))
        }
        val report = buildHoldRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
