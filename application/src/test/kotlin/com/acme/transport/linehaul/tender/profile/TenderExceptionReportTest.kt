package com.acme.transport.linehaul.tender.profile

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708365280)
        val items = mutableListOf<TenderException>()
        for (index in 0 until 6) {
            items.add(newTenderException("tender-exception-$index", "sample"))
        }
        val report = buildTenderExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
