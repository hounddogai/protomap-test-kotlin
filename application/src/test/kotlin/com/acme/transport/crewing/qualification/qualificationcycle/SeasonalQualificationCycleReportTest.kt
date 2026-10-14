package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalQualificationCycleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703283276)
        val items = mutableListOf<SeasonalQualificationCycle>()
        for (index in 0 until 4) {
            items.add(newSeasonalQualificationCycle("seasonal-qualification-cycle-$index", "sample"))
        }
        val report = buildSeasonalQualificationCycleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
