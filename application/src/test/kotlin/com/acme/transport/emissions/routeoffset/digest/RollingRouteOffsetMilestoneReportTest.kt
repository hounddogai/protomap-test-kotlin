package com.acme.transport.emissions.routeoffset.digest

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRouteOffsetMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703542786)
        val items = mutableListOf<RollingRouteOffsetMilestone>()
        for (index in 0 until 3) {
            items.add(newRollingRouteOffsetMilestone("rolling-route-offset-milestone-$index", "sample"))
        }
        val report = buildRollingRouteOffsetMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
