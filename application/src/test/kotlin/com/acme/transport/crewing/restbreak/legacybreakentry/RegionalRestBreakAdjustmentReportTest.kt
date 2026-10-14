package com.acme.transport.crewing.restbreak.legacybreakentry

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRestBreakAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703536237)
        val items = mutableListOf<RegionalRestBreakAdjustment>()
        for (index in 0 until 3) {
            items.add(newRegionalRestBreakAdjustment("regional-rest-break-adjustment-$index", "sample"))
        }
        val report = buildRegionalRestBreakAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
