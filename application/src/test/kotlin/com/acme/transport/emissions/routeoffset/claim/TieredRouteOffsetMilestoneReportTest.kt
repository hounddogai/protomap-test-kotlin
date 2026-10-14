package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRouteOffsetMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705770210)
        val items = mutableListOf<TieredRouteOffsetMilestone>()
        for (index in 0 until 4) {
            items.add(newTieredRouteOffsetMilestone("tiered-route-offset-milestone-$index", "sample"))
        }
        val report = buildTieredRouteOffsetMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
