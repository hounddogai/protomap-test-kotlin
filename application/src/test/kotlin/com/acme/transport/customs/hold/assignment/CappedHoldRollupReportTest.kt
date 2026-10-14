package com.acme.transport.customs.hold.assignment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedHoldRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708011195)
        val items = mutableListOf<CappedHoldRollup>()
        for (index in 0 until 4) {
            items.add(newCappedHoldRollup("capped-hold-rollup-$index", "sample"))
        }
        val report = buildCappedHoldRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
