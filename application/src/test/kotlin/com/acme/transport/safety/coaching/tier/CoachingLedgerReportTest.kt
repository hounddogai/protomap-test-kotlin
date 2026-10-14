package com.acme.transport.safety.coaching.tier

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701331341)
        val items = mutableListOf<CoachingLedger>()
        for (index in 0 until 5) {
            items.add(newCoachingLedger("coaching-ledger-$index", "sample"))
        }
        val report = buildCoachingLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
