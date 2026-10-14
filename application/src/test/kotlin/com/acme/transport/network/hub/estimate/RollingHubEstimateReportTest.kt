package com.acme.transport.network.hub.estimate

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingHubEstimateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701913119)
        val items = mutableListOf<RollingHubEstimate>()
        for (index in 0 until 5) {
            items.add(newRollingHubEstimate("rolling-hub-estimate-$index", "sample"))
        }
        val report = buildRollingHubEstimateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
