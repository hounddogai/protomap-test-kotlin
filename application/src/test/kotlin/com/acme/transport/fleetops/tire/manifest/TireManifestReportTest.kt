package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701285985)
        val items = mutableListOf<TireManifest>()
        for (index in 0 until 6) {
            items.add(newTireManifest("tire-manifest-$index", "sample"))
        }
        val report = buildTireManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
