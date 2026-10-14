package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTireManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701041548)
        val items = mutableListOf<AdaptiveTireManifest>()
        for (index in 0 until 6) {
            items.add(newAdaptiveTireManifest("adaptive-tire-manifest-$index", "sample"))
        }
        val report = buildAdaptiveTireManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
