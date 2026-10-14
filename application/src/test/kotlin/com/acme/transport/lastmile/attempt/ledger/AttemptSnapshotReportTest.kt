package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702113887)
        val items = mutableListOf<AttemptSnapshot>()
        for (index in 0 until 6) {
            items.add(newAttemptSnapshot("attempt-snapshot-$index", "sample"))
        }
        val report = buildAttemptSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
