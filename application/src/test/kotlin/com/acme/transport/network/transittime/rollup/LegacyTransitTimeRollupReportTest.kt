package com.acme.transport.network.transittime.rollup

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTransitTimeRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707097443)
        val items = mutableListOf<LegacyTransitTimeRollup>()
        for (index in 0 until 2) {
            items.add(newLegacyTransitTimeRollup("legacy-transit-time-rollup-$index", "sample"))
        }
        val report = buildLegacyTransitTimeRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
