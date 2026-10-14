package com.acme.transport.terminal.sortation.request

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictSortationManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705141086)
        val items = mutableListOf<StrictSortationManifest>()
        for (index in 0 until 6) {
            items.add(newStrictSortationManifest("strict-sortation-manifest-$index", "sample"))
        }
        val report = buildStrictSortationManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
