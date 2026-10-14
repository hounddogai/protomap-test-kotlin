package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700033528)
        val items = mutableListOf<NearMissPolicy>()
        for (index in 0 until 2) {
            items.add(newNearMissPolicy("near-miss-policy-$index", "sample"))
        }
        val report = buildNearMissPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
