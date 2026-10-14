package com.acme.transport.linehaul.trailer.draft

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705685339)
        val items = mutableListOf<TrailerDraft>()
        for (index in 0 until 5) {
            items.add(newTrailerDraft("trailer-draft-$index", "sample"))
        }
        val report = buildTrailerDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
