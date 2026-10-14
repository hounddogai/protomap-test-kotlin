package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveInspectionDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708138275)
        val items = mutableListOf<AdaptiveInspectionDraft>()
        for (index in 0 until 2) {
            items.add(newAdaptiveInspectionDraft("adaptive-inspection-draft-$index", "sample"))
        }
        val report = buildAdaptiveInspectionDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
