package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAccessorialAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704405075)
        val items = mutableListOf<SeasonalAccessorialAllocation>()
        for (index in 0 until 4) {
            items.add(newSeasonalAccessorialAllocation("seasonal-accessorial-allocation-$index", "sample"))
        }
        val report = buildSeasonalAccessorialAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
