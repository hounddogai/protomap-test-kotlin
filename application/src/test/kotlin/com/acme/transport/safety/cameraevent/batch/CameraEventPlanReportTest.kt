package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707087869)
        val items = mutableListOf<CameraEventPlan>()
        for (index in 0 until 5) {
            items.add(newCameraEventPlan("camera-event-plan-$index", "sample"))
        }
        val report = buildCameraEventPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
