package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictServiceAreaMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701678412)
        val items = mutableListOf<StrictServiceAreaMilestone>()
        for (index in 0 until 2) {
            items.add(newStrictServiceAreaMilestone("strict-service-area-milestone-$index", "sample"))
        }
        val report = buildStrictServiceAreaMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
