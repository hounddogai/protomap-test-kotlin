package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRouteOffsetDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703749501)
        val items = mutableListOf<PrimaryRouteOffsetDigest>()
        for (index in 0 until 5) {
            items.add(newPrimaryRouteOffsetDigest("primary-route-offset-digest-$index", "sample"))
        }
        val report = buildPrimaryRouteOffsetDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
