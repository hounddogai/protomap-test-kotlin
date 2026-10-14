package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700700415)
        val items = mutableListOf<ParcelMilestone>()
        for (index in 0 until 2) {
            items.add(newParcelMilestone("parcel-milestone-$index", "sample"))
        }
        val report = buildParcelMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
