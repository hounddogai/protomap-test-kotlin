package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedTenderWindowReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703125428)
        val items = mutableListOf<CappedTenderWindow>()
        for (index in 0 until 3) {
            items.add(newCappedTenderWindow("capped-tender-window-$index", "sample"))
        }
        val report = buildCappedTenderWindowReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
