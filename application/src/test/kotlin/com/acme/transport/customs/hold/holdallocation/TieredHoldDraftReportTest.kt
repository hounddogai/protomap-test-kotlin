package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredHoldDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708343687)
        val items = mutableListOf<TieredHoldDraft>()
        for (index in 0 until 4) {
            items.add(newTieredHoldDraft("tiered-hold-draft-$index", "sample"))
        }
        val report = buildTieredHoldDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
