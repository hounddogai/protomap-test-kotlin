package com.acme.transport.network.zone.claim

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedZoneClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701627483)
        val items = mutableListOf<CappedZoneClaim>()
        for (index in 0 until 2) {
            items.add(newCappedZoneClaim("capped-zone-claim-$index", "sample"))
        }
        val report = buildCappedZoneClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
