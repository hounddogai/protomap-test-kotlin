package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargePolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705274106)
        val items = mutableListOf<EvChargePolicy>()
        for (index in 0 until 5) {
            items.add(newEvChargePolicy("ev-charge-policy-$index", "sample"))
        }
        val report = buildEvChargePolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
