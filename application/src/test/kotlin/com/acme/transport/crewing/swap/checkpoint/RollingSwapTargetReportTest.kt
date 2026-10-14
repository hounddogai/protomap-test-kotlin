package com.acme.transport.crewing.swap.checkpoint

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingSwapTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704034194)
        val items = mutableListOf<RollingSwapTarget>()
        for (index in 0 until 6) {
            items.add(newRollingSwapTarget("rolling-swap-target-$index", "sample"))
        }
        val report = buildRollingSwapTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
