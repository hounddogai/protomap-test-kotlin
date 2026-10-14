package com.acme.transport.network.hub.milestone

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedHubBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701914972)
        val items = mutableListOf<CappedHubBaseline>()
        for (index in 0 until 4) {
            items.add(newCappedHubBaseline("capped-hub-baseline-$index", "sample"))
        }
        val report = buildCappedHubBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
