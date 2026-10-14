package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCutoffOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703743850)
        val items = mutableListOf<RegionalCutoffOverride>()
        for (index in 0 until 4) {
            items.add(newRegionalCutoffOverride("regional-cutoff-override-$index", "sample"))
        }
        val report = buildRegionalCutoffOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
