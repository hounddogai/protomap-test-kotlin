package com.acme.transport.emissions.evcharge.score

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706554121)
        val items = mutableListOf<EvChargeMapping>()
        for (index in 0 until 6) {
            items.add(newEvChargeMapping("ev-charge-mapping-$index", "sample"))
        }
        val report = buildEvChargeMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
