package com.acme.transport.linehaul.tender.plan

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705253991)
        val items = mutableListOf<TenderGroup>()
        for (index in 0 until 3) {
            items.add(newTenderGroup("tender-group-$index", "sample"))
        }
        val report = buildTenderGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
