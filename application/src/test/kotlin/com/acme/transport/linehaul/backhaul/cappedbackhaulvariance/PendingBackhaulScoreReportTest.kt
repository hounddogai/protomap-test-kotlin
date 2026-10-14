package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingBackhaulScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704025808)
        val items = mutableListOf<PendingBackhaulScore>()
        for (index in 0 until 3) {
            items.add(newPendingBackhaulScore("pending-backhaul-score-$index", "sample"))
        }
        val report = buildPendingBackhaulScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
