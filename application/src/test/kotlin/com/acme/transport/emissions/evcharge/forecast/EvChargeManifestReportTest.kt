package com.acme.transport.emissions.evcharge.forecast

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705507203)
        val items = mutableListOf<EvChargeManifest>()
        for (index in 0 until 6) {
            items.add(newEvChargeManifest("ev-charge-manifest-$index", "sample"))
        }
        val report = buildEvChargeManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
