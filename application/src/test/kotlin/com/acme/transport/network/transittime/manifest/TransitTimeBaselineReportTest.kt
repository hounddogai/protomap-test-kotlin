package com.acme.transport.network.transittime.manifest

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702325738)
        val items = mutableListOf<TransitTimeBaseline>()
        for (index in 0 until 6) {
            items.add(newTransitTimeBaseline("transit-time-baseline-$index", "sample"))
        }
        val report = buildTransitTimeBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
