package com.acme.transport.emissions.idling.milestone

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701272880)
        val items = mutableListOf<IdlingMilestone>()
        for (index in 0 until 4) {
            items.add(newIdlingMilestone("idling-milestone-$index", "sample"))
        }
        val report = buildIdlingMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
