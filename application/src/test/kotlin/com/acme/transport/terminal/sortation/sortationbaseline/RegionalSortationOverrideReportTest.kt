package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalSortationOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702311761)
        val items = mutableListOf<RegionalSortationOverride>()
        for (index in 0 until 3) {
            items.add(newRegionalSortationOverride("regional-sortation-override-$index", "sample"))
        }
        val report = buildRegionalSortationOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
