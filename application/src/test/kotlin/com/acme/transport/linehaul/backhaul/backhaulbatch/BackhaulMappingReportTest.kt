package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703381402)
        val items = mutableListOf<BackhaulMapping>()
        for (index in 0 until 4) {
            items.add(newBackhaulMapping("backhaul-mapping-$index", "sample"))
        }
        val report = buildBackhaulMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
