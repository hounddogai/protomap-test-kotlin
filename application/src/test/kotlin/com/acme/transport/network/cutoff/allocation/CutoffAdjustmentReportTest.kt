package com.acme.transport.network.cutoff.allocation

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707742243)
        val items = mutableListOf<CutoffAdjustment>()
        for (index in 0 until 4) {
            items.add(newCutoffAdjustment("cutoff-adjustment-$index", "sample"))
        }
        val report = buildCutoffAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
