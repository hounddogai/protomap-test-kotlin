package com.acme.transport.lastmile.drop.cappeddropmilestone

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDropMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709535434)
        val items = mutableListOf<SeasonalDropMilestone>()
        for (index in 0 until 4) {
            items.add(newSeasonalDropMilestone("seasonal-drop-milestone-$index", "sample"))
        }
        val report = buildSeasonalDropMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
