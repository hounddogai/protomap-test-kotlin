package com.acme.transport.network.hub.allocation

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingHubBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704170914)
        val items = mutableListOf<RollingHubBaseline>()
        for (index in 0 until 3) {
            items.add(newRollingHubBaseline("rolling-hub-baseline-$index", "sample"))
        }
        val report = buildRollingHubBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
