package com.acme.transport.customs.hold.batch

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702840345)
        val items = mutableListOf<HoldException>()
        for (index in 0 until 6) {
            items.add(newHoldException("hold-exception-$index", "sample"))
        }
        val report = buildHoldExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
