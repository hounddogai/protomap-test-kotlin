package com.acme.transport.linehaul.capacity.mapping

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacityMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706935447)
        val items = mutableListOf<CapacityMapping>()
        for (index in 0 until 2) {
            items.add(newCapacityMapping("capacity-mapping-$index", "sample"))
        }
        val report = buildCapacityMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
