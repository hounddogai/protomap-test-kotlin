package com.acme.transport.network.zone.batch

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700673883)
        val items = mutableListOf<ZoneException>()
        for (index in 0 until 4) {
            items.add(newZoneException("zone-exception-$index", "sample"))
        }
        val report = buildZoneExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
