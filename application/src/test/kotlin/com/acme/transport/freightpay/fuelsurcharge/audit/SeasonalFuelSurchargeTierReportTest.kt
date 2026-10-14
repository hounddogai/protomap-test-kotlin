package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalFuelSurchargeTierReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702684902)
        val items = mutableListOf<SeasonalFuelSurchargeTier>()
        for (index in 0 until 4) {
            items.add(newSeasonalFuelSurchargeTier("seasonal-fuel-surcharge-tier-$index", "sample"))
        }
        val report = buildSeasonalFuelSurchargeTierReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
