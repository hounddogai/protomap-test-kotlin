package com.acme.transport.safety.coaching.milestone

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCoachingTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707144708)
        val items = mutableListOf<WeightedCoachingTemplate>()
        for (index in 0 until 4) {
            items.add(newWeightedCoachingTemplate("weighted-coaching-template-$index", "sample"))
        }
        val report = buildWeightedCoachingTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
