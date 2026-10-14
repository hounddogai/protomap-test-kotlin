package com.acme.transport.lastmile.locker.score

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLockerScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706514858)
        val items = mutableListOf<CappedLockerScore>()
        for (index in 0 until 2) {
            items.add(newCappedLockerScore("capped-locker-score-$index", "sample"))
        }
        val report = buildCappedLockerScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
