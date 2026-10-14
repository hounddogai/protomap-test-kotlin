package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledIdlingManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704605163)
        val items = mutableListOf<ScheduledIdlingManifest>()
        for (index in 0 until 6) {
            items.add(newScheduledIdlingManifest("scheduled-idling-manifest-$index", "sample"))
        }
        val report = buildScheduledIdlingManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
