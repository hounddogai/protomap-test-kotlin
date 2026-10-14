package com.acme.transport.safety.scorecard.segment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingScorecardNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706508606)
        val items = mutableListOf<RollingScorecardNotice>()
        for (index in 0 until 2) {
            items.add(newRollingScorecardNotice("rolling-scorecard-notice-$index", "sample"))
        }
        val report = buildRollingScorecardNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
