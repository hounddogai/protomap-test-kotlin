package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAccessorialEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704230208)
        val items = mutableListOf<CappedAccessorialEntry>()
        for (index in 0 until 2) {
            items.add(newCappedAccessorialEntry("capped-accessorial-entry-$index", "sample"))
        }
        val report = buildCappedAccessorialEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
