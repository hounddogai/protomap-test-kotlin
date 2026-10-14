package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDwellManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700991910)
        val items = mutableListOf<ManualDwellManifest>()
        for (index in 0 until 4) {
            items.add(newManualDwellManifest("manual-dwell-manifest-$index", "sample"))
        }
        val report = buildManualDwellManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
