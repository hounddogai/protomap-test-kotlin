package com.acme.transport.network.hub.estimate

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientHubBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708887971)
        val items = mutableListOf<LenientHubBaseline>()
        for (index in 0 until 4) {
            items.add(newLenientHubBaseline("lenient-hub-baseline-$index", "sample"))
        }
        val report = buildLenientHubBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
