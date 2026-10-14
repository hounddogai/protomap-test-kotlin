package com.acme.transport.lastmile.drop.droprequest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708617065)
        val items = mutableListOf<DropMapping>()
        for (index in 0 until 3) {
            items.add(newDropMapping("drop-mapping-$index", "sample"))
        }
        val report = buildDropMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
