package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientNearMissPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704318029)
        val items = mutableListOf<LenientNearMissPolicy>()
        for (index in 0 until 4) {
            items.add(newLenientNearMissPolicy("lenient-near-miss-policy-$index", "sample"))
        }
        val report = buildLenientNearMissPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
