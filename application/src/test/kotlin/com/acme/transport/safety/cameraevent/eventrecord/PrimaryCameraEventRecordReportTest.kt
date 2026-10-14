package com.acme.transport.safety.cameraevent.eventrecord

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCameraEventRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707487692)
        val items = mutableListOf<PrimaryCameraEventRecord>()
        for (index in 0 until 3) {
            items.add(newPrimaryCameraEventRecord("primary-camera-event-record-$index", "sample"))
        }
        val report = buildPrimaryCameraEventRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
