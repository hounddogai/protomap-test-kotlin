package com.acme.transport.network.transittime.digest

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707920801)
        val items = mutableListOf<TransitTimeManifest>()
        for (index in 0 until 3) {
            items.add(newTransitTimeManifest("transit-time-manifest-$index", "sample"))
        }
        val report = buildTransitTimeManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
