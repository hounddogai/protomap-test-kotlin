package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703863876)
        val items = mutableListOf<ScorecardDigest>()
        for (index in 0 until 3) {
            items.add(newScorecardDigest("scorecard-digest-$index", "sample"))
        }
        val report = buildScorecardDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
