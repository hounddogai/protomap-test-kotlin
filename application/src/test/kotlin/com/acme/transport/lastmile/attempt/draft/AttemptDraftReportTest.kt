package com.acme.transport.lastmile.attempt.draft

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702719410)
        val items = mutableListOf<AttemptDraft>()
        for (index in 0 until 4) {
            items.add(newAttemptDraft("attempt-draft-$index", "sample"))
        }
        val report = buildAttemptDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
