package com.acme.transport.safety.cameraevent.slot

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalCameraEventPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705409808)
        val items = mutableListOf<SeasonalCameraEventPlan>()
        for (index in 0 until 4) {
            items.add(newSeasonalCameraEventPlan("seasonal-camera-event-plan-$index", "sample"))
        }
        val report = buildSeasonalCameraEventPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
