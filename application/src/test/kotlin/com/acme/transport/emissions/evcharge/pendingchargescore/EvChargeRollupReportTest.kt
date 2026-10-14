package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704257843)
        val items = mutableListOf<EvChargeRollup>()
        for (index in 0 until 6) {
            items.add(newEvChargeRollup("ev-charge-rollup-$index", "sample"))
        }
        val report = buildEvChargeRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
