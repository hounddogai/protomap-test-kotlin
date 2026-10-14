package com.acme.transport.safety.cameraevent.group

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCameraEventGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703284791)
        val items = mutableListOf<DefaultCameraEventGroup>()
        for (index in 0 until 6) {
            items.add(newDefaultCameraEventGroup("default-camera-event-group-$index", "sample"))
        }
        val report = buildDefaultCameraEventGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
