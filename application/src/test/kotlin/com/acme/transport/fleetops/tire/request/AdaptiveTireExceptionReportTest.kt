package com.acme.transport.fleetops.tire.request

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTireExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701611760)
        val items = mutableListOf<AdaptiveTireException>()
        for (index in 0 until 4) {
            items.add(newAdaptiveTireException("adaptive-tire-exception-$index", "sample"))
        }
        val report = buildAdaptiveTireExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
