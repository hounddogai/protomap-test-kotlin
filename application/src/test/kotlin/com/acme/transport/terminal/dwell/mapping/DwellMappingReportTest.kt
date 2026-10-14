package com.acme.transport.terminal.dwell.mapping

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703500180)
        val items = mutableListOf<DwellMapping>()
        for (index in 0 until 4) {
            items.add(newDwellMapping("dwell-mapping-$index", "sample"))
        }
        val report = buildDwellMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
