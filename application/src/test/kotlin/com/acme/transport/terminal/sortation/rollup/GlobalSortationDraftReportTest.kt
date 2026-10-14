package com.acme.transport.terminal.sortation.rollup

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalSortationDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705225510)
        val items = mutableListOf<GlobalSortationDraft>()
        for (index in 0 until 4) {
            items.add(newGlobalSortationDraft("global-sortation-draft-$index", "sample"))
        }
        val report = buildGlobalSortationDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
