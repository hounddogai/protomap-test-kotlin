package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCutoffDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705322502)
        val items = mutableListOf<ManualCutoffDraft>()
        for (index in 0 until 2) {
            items.add(newManualCutoffDraft("manual-cutoff-draft-$index", "sample"))
        }
        val report = buildManualCutoffDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
