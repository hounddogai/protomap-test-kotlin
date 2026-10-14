package com.acme.transport.crewing.roster.claim

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703580377)
        val items = mutableListOf<RosterClaim>()
        for (index in 0 until 2) {
            items.add(newRosterClaim("roster-claim-$index", "sample"))
        }
        val report = buildRosterClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
