package com.acme.transport.safety.cameraevent.evententry

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCameraEventDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700600235)
        val items = mutableListOf<PrimaryCameraEventDigest>()
        for (index in 0 until 2) {
            items.add(newPrimaryCameraEventDigest("primary-camera-event-digest-$index", "sample"))
        }
        val report = buildPrimaryCameraEventDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
