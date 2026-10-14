package com.acme.transport.lastmile.attempt.attemptprofile

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAttemptClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703735676)
        val items = mutableListOf<GlobalAttemptClaim>()
        for (index in 0 until 2) {
            items.add(newGlobalAttemptClaim("global-attempt-claim-$index", "sample"))
        }
        val report = buildGlobalAttemptClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
