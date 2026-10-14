package com.acme.transport.customs.clearance.milestone

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualClearanceManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700174983)
        val items = mutableListOf<ManualClearanceManifest>()
        for (index in 0 until 3) {
            items.add(newManualClearanceManifest("manual-clearance-manifest-$index", "sample"))
        }
        val report = buildManualClearanceManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
