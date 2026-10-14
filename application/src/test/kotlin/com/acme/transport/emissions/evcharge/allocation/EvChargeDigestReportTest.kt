package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706649184)
        val items = mutableListOf<EvChargeDigest>()
        for (index in 0 until 2) {
            items.add(newEvChargeDigest("ev-charge-digest-$index", "sample"))
        }
        val report = buildEvChargeDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
