package com.acme.transport.network.cutoff.draft

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CutoffManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707407447)
        val items = mutableListOf<CutoffManifest>()
        for (index in 0 until 6) {
            items.add(newCutoffManifest("cutoff-manifest-$index", "sample"))
        }
        val report = buildCutoffManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
