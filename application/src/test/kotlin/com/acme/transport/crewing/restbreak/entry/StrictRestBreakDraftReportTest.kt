package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRestBreakDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703570374)
        val items = mutableListOf<StrictRestBreakDraft>()
        for (index in 0 until 5) {
            items.add(newStrictRestBreakDraft("strict-rest-break-draft-$index", "sample"))
        }
        val report = buildStrictRestBreakDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
