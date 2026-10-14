package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredFuelSurchargeSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709803180)
        val items = mutableListOf<TieredFuelSurchargeSummary>()
        for (index in 0 until 6) {
            items.add(newTieredFuelSurchargeSummary("tiered-fuel-surcharge-summary-$index", "sample"))
        }
        val report = buildTieredFuelSurchargeSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
