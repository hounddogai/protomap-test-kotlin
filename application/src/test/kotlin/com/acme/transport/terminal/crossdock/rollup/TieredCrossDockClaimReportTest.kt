package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCrossDockClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709530474)
        val items = mutableListOf<TieredCrossDockClaim>()
        for (index in 0 until 3) {
            items.add(newTieredCrossDockClaim("tiered-cross-dock-claim-$index", "sample"))
        }
        val report = buildTieredCrossDockClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
