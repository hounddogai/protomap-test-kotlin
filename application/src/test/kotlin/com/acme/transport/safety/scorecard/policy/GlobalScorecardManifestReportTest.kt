package com.acme.transport.safety.scorecard.policy

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalScorecardManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701667059)
        val items = mutableListOf<GlobalScorecardManifest>()
        for (index in 0 until 2) {
            items.add(newGlobalScorecardManifest("global-scorecard-manifest-$index", "sample"))
        }
        val report = buildGlobalScorecardManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
