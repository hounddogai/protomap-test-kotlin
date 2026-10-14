package com.acme.transport.crewing.swap.swapclaim

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimarySwapMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701327336)
        val items = mutableListOf<PrimarySwapMapping>()
        for (index in 0 until 2) {
            items.add(newPrimarySwapMapping("primary-swap-mapping-$index", "sample"))
        }
        val report = buildPrimarySwapMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
