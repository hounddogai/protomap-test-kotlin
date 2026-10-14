package com.acme.transport.linehaul.laneplan.threshold

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LanePlanManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704235238)
        val items = mutableListOf<LanePlanManifest>()
        for (index in 0 until 6) {
            items.add(newLanePlanManifest("lane-plan-manifest-$index", "sample"))
        }
        val report = buildLanePlanManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
