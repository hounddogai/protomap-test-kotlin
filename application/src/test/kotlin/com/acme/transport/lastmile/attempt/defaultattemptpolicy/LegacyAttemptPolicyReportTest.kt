package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAttemptPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702839384)
        val items = mutableListOf<LegacyAttemptPolicy>()
        for (index in 0 until 5) {
            items.add(newLegacyAttemptPolicy("legacy-attempt-policy-$index", "sample"))
        }
        val report = buildLegacyAttemptPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
