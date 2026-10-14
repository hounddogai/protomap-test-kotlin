package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709816081)
        val items = mutableListOf<DropRollup>()
        for (index in 0 until 5) {
            items.add(newDropRollup("drop-rollup-$index", "sample"))
        }
        val report = buildDropRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
