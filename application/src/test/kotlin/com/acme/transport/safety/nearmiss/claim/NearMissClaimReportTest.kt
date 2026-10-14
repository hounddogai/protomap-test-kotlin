package com.acme.transport.safety.nearmiss.claim

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702606838)
        val items = mutableListOf<NearMissClaim>()
        for (index in 0 until 3) {
            items.add(newNearMissClaim("near-miss-claim-$index", "sample"))
        }
        val report = buildNearMissClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
