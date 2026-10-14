package com.acme.transport.lastmile.attempt.window

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAttemptClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708481947)
        val items = mutableListOf<DefaultAttemptClaim>()
        for (index in 0 until 4) {
            items.add(newDefaultAttemptClaim("default-attempt-claim-$index", "sample"))
        }
        val report = buildDefaultAttemptClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
