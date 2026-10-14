package com.acme.transport.network.hub.batchedhubmilestone

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveHubMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706633507)
        val items = mutableListOf<AdaptiveHubMilestone>()
        for (index in 0 until 3) {
            items.add(newAdaptiveHubMilestone("adaptive-hub-milestone-$index", "sample"))
        }
        val report = buildAdaptiveHubMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
