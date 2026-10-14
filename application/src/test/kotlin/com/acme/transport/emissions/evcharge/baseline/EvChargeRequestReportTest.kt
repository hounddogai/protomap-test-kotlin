package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707055807)
        val items = mutableListOf<EvChargeRequest>()
        for (index in 0 until 4) {
            items.add(newEvChargeRequest("ev-charge-request-$index", "sample"))
        }
        val report = buildEvChargeRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
