package com.acme.transport.network.cutoff.target

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCutoffTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706181667)
        val items = mutableListOf<RegionalCutoffTarget>()
        for (index in 0 until 2) {
            items.add(newRegionalCutoffTarget("regional-cutoff-target-$index", "sample"))
        }
        val report = buildRegionalCutoffTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
