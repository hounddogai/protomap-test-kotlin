package com.acme.transport.terminal.sortation.manifest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacySortationManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707999957)
        val items = mutableListOf<LegacySortationManifest>()
        for (index in 0 until 4) {
            items.add(newLegacySortationManifest("legacy-sortation-manifest-$index", "sample"))
        }
        val report = buildLegacySortationManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
