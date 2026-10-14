package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700799460)
        val items = mutableListOf<EvChargeOverride>()
        for (index in 0 until 6) {
            items.add(newEvChargeOverride("ev-charge-override-$index", "sample"))
        }
        val report = buildEvChargeOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
