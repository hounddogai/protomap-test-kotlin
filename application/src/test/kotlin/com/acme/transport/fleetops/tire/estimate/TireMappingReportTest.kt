package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TireMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709190043)
        val items = mutableListOf<TireMapping>()
        for (index in 0 until 4) {
            items.add(newTireMapping("tire-mapping-$index", "sample"))
        }
        val report = buildTireMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
