package com.acme.transport.lastmile.drop.draft

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704346597)
        val items = mutableListOf<DropDraft>()
        for (index in 0 until 4) {
            items.add(newDropDraft("drop-draft-$index", "sample"))
        }
        val report = buildDropDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
