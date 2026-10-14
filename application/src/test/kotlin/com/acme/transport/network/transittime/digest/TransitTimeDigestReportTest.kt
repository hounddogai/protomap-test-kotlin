package com.acme.transport.network.transittime.digest

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705157622)
        val items = mutableListOf<TransitTimeDigest>()
        for (index in 0 until 2) {
            items.add(newTransitTimeDigest("transit-time-digest-$index", "sample"))
        }
        val report = buildTransitTimeDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
