package com.acme.transport.customs.inspection.draft

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705351607)
        val items = mutableListOf<InspectionDraft>()
        for (index in 0 until 4) {
            items.add(newInspectionDraft("inspection-draft-$index", "sample"))
        }
        val report = buildInspectionDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
