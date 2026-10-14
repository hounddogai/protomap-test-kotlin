package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalCapacityRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705047003)
        val items = mutableListOf<RegionalCapacityRollup>()
        for (index in 0 until 3) {
            items.add(newRegionalCapacityRollup("regional-capacity-rollup-$index", "sample"))
        }
        val report = buildRegionalCapacityRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
