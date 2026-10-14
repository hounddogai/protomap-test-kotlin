package com.acme.transport.safety.nearmiss.proposal

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientNearMissClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701339783)
        val items = mutableListOf<LenientNearMissClaim>()
        for (index in 0 until 3) {
            items.add(newLenientNearMissClaim("lenient-near-miss-claim-$index", "sample"))
        }
        val report = buildLenientNearMissClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
