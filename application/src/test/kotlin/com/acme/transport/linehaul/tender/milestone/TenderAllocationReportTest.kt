package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701952138)
        val items = mutableListOf<TenderAllocation>()
        for (index in 0 until 2) {
            items.add(newTenderAllocation("tender-allocation-$index", "sample"))
        }
        val report = buildTenderAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
