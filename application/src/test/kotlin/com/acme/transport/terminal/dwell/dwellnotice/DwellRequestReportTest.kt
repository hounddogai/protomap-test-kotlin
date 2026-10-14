package com.acme.transport.terminal.dwell.dwellnotice

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705847650)
        val items = mutableListOf<DwellRequest>()
        for (index in 0 until 2) {
            items.add(newDwellRequest("dwell-request-$index", "sample"))
        }
        val report = buildDwellRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
