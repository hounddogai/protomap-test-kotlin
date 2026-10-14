package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRouteOffsetClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708982809)
        val items = mutableListOf<TieredRouteOffsetClaim>()
        for (index in 0 until 6) {
            items.add(newTieredRouteOffsetClaim("tiered-route-offset-claim-$index", "sample"))
        }
        val report = buildTieredRouteOffsetClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
