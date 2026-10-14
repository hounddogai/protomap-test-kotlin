package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCameraEventEntryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702565063)
        val items = mutableListOf<CappedCameraEventEntry>()
        for (index in 0 until 4) {
            items.add(newCappedCameraEventEntry("capped-camera-event-entry-$index", "sample"))
        }
        val report = buildCappedCameraEventEntryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
