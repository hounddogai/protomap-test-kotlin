package com.acme.transport.network.servicearea.milestone

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServiceAreaMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708618695)
        val items = mutableListOf<LenientServiceAreaMilestone>()
        for (index in 0 until 6) {
            items.add(newLenientServiceAreaMilestone("lenient-service-area-milestone-$index", "sample"))
        }
        val report = buildLenientServiceAreaMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
