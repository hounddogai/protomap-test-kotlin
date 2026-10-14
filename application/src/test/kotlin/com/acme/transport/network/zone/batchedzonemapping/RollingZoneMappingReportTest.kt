package com.acme.transport.network.zone.batchedzonemapping

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingZoneMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701280054)
        val items = mutableListOf<RollingZoneMapping>()
        for (index in 0 until 3) {
            items.add(newRollingZoneMapping("rolling-zone-mapping-$index", "sample"))
        }
        val report = buildRollingZoneMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
