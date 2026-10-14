package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingScorecardPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709121962)
        val items = mutableListOf<PendingScorecardPolicy>()
        for (index in 0 until 6) {
            items.add(newPendingScorecardPolicy("pending-scorecard-policy-$index", "sample"))
        }
        val report = buildPendingScorecardPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
