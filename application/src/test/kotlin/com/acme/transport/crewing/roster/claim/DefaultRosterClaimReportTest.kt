package com.acme.transport.crewing.roster.claim

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultRosterClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705967187)
        val items = mutableListOf<DefaultRosterClaim>()
        for (index in 0 until 5) {
            items.add(newDefaultRosterClaim("default-roster-claim-$index", "sample"))
        }
        val report = buildDefaultRosterClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
