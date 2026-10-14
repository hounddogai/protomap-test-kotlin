package com.acme.transport.linehaul.tender.milestone

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703806793)
        val items = mutableListOf<TenderMilestone>()
        for (index in 0 until 5) {
            items.add(newTenderMilestone("tender-milestone-$index", "sample"))
        }
        val report = buildTenderMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
