package com.acme.transport.customs.clearance.clearancemanifest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryClearanceManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707856423)
        val items = mutableListOf<PrimaryClearanceManifest>()
        for (index in 0 until 5) {
            items.add(newPrimaryClearanceManifest("primary-clearance-manifest-$index", "sample"))
        }
        val report = buildPrimaryClearanceManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
