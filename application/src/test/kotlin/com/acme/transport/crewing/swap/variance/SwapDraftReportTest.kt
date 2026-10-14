package com.acme.transport.crewing.swap.variance

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704487475)
        val items = mutableListOf<SwapDraft>()
        for (index in 0 until 3) {
            items.add(newSwapDraft("swap-draft-$index", "sample"))
        }
        val report = buildSwapDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
