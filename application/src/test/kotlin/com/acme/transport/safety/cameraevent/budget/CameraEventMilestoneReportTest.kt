package com.acme.transport.safety.cameraevent.budget

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702059532)
        val items = mutableListOf<CameraEventMilestone>()
        for (index in 0 until 4) {
            items.add(newCameraEventMilestone("camera-event-milestone-$index", "sample"))
        }
        val report = buildCameraEventMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
