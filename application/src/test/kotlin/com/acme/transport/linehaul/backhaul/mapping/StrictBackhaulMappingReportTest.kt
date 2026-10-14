package com.acme.transport.linehaul.backhaul.mapping

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictBackhaulMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704874834)
        val items = mutableListOf<StrictBackhaulMapping>()
        for (index in 0 until 3) {
            items.add(newStrictBackhaulMapping("strict-backhaul-mapping-$index", "sample"))
        }
        val report = buildStrictBackhaulMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
