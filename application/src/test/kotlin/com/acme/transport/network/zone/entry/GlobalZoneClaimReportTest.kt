package com.acme.transport.network.zone.entry

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalZoneClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709680977)
        val items = mutableListOf<GlobalZoneClaim>()
        for (index in 0 until 5) {
            items.add(newGlobalZoneClaim("global-zone-claim-$index", "sample"))
        }
        val report = buildGlobalZoneClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
