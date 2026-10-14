package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRestBreakClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702405196)
        val items = mutableListOf<StrictRestBreakClaim>()
        for (index in 0 until 3) {
            items.add(newStrictRestBreakClaim("strict-rest-break-claim-$index", "sample"))
        }
        val report = buildStrictRestBreakClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
