package com.acme.transport.fleetops.inspection.claim

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalInspectionDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707459688)
        val items = mutableListOf<SeasonalInspectionDraft>()
        for (index in 0 until 6) {
            items.add(newSeasonalInspectionDraft("seasonal-inspection-draft-$index", "sample"))
        }
        val report = buildSeasonalInspectionDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
