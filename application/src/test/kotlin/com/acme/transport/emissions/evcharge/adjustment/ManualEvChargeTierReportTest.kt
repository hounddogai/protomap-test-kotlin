package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualEvChargeTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701662703)
        val items = mutableListOf<ManualEvChargeTier>()
        for (index in 0 until 6) {
            items.add(newManualEvChargeTier("manual-ev-charge-tier-$index", "sample"))
        }
        val report = buildManualEvChargeTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
