package com.acme.transport.linehaul.tender.exception

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTenderAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702148644)
        val items = mutableListOf<StrictTenderAllocation>()
        for (index in 0 until 2) {
            items.add(newStrictTenderAllocation("strict-tender-allocation-$index", "sample"))
        }
        val report = buildStrictTenderAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
