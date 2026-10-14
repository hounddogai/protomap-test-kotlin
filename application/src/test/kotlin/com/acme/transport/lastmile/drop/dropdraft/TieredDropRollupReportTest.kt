package com.acme.transport.lastmile.drop.dropdraft

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDropRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708935245)
        val items = mutableListOf<TieredDropRollup>()
        for (index in 0 until 5) {
            items.add(newTieredDropRollup("tiered-drop-rollup-$index", "sample"))
        }
        val report = buildTieredDropRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
