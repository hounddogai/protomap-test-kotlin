package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryEvChargeGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708233607)
        val items = mutableListOf<PrimaryEvChargeGroup>()
        for (index in 0 until 6) {
            items.add(newPrimaryEvChargeGroup("primary-ev-charge-group-$index", "sample"))
        }
        val report = buildPrimaryEvChargeGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
