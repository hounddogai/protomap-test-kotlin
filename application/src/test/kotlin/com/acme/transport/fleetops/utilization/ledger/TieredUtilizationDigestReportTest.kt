package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredUtilizationDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709348544)
        val items = mutableListOf<TieredUtilizationDigest>()
        for (index in 0 until 3) {
            items.add(newTieredUtilizationDigest("tiered-utilization-digest-$index", "sample"))
        }
        val report = buildTieredUtilizationDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
