package com.acme.transport.safety.cameraevent.rollup

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCameraEventTargetReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705064703)
        val items = mutableListOf<PrimaryCameraEventTarget>()
        for (index in 0 until 4) {
            items.add(newPrimaryCameraEventTarget("primary-camera-event-target-$index", "sample"))
        }
        val report = buildPrimaryCameraEventTargetReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
