package com.acme.transport.network.transittime.claim

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTransitTimeDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707298171)
        val items = mutableListOf<StrictTransitTimeDigest>()
        for (index in 0 until 6) {
            items.add(newStrictTransitTimeDigest("strict-transit-time-digest-$index", "sample"))
        }
        val report = buildStrictTransitTimeDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
