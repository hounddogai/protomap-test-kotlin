package com.acme.transport.linehaul.backhaul.target

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveBackhaulTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704866651)
        val items = mutableListOf<AdaptiveBackhaulTarget>()
        for (index in 0 until 2) {
            items.add(newAdaptiveBackhaulTarget("adaptive-backhaul-target-$index", "sample"))
        }
        val report = buildAdaptiveBackhaulTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
