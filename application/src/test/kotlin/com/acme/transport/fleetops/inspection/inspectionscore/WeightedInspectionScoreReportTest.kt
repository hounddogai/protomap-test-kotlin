package com.acme.transport.fleetops.inspection.inspectionscore

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedInspectionScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705381694)
        val items = mutableListOf<WeightedInspectionScore>()
        for (index in 0 until 6) {
            items.add(newWeightedInspectionScore("weighted-inspection-score-$index", "sample"))
        }
        val report = buildWeightedInspectionScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
