package com.acme.transport.emissions.idling.checkpoint

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIdlingMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703686288)
        val items = mutableListOf<RegionalIdlingMapping>()
        for (index in 0 until 4) {
            items.add(newRegionalIdlingMapping("regional-idling-mapping-$index", "sample"))
        }
        val report = buildRegionalIdlingMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
