package com.acme.transport.safety.nearmiss.segment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalNearMissRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708632284)
        val items = mutableListOf<GlobalNearMissRequest>()
        for (index in 0 until 6) {
            items.add(newGlobalNearMissRequest("global-near-miss-request-$index", "sample"))
        }
        val report = buildGlobalNearMissRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
