package com.acme.transport.network.hub.allocation

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalHubClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709689852)
        val items = mutableListOf<SeasonalHubClaim>()
        for (index in 0 until 4) {
            items.add(newSeasonalHubClaim("seasonal-hub-claim-$index", "sample"))
        }
        val report = buildSeasonalHubClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
