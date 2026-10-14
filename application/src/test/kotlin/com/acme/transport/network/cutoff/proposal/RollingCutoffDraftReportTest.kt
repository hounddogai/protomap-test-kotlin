package com.acme.transport.network.cutoff.proposal

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingCutoffDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707764517)
        val items = mutableListOf<RollingCutoffDraft>()
        for (index in 0 until 4) {
            items.add(newRollingCutoffDraft("rolling-cutoff-draft-$index", "sample"))
        }
        val report = buildRollingCutoffDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
