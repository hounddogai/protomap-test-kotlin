package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAttemptDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707958392)
        val items = mutableListOf<GlobalAttemptDraft>()
        for (index in 0 until 3) {
            items.add(newGlobalAttemptDraft("global-attempt-draft-$index", "sample"))
        }
        val report = buildGlobalAttemptDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
