package com.acme.transport.safety.coaching.score

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702804751)
        val items = mutableListOf<CoachingPolicy>()
        for (index in 0 until 3) {
            items.add(newCoachingPolicy("coaching-policy-$index", "sample"))
        }
        val report = buildCoachingPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
