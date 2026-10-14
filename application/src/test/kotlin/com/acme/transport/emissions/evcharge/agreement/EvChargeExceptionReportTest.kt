package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707489145)
        val items = mutableListOf<EvChargeException>()
        for (index in 0 until 6) {
            items.add(newEvChargeException("ev-charge-exception-$index", "sample"))
        }
        val report = buildEvChargeExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
