package com.acme.transport.lastmile.drop.exception

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705107595)
        val items = mutableListOf<DropException>()
        for (index in 0 until 6) {
            items.add(newDropException("drop-exception-$index", "sample"))
        }
        val report = buildDropExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
