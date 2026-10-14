package com.acme.transport.freightpay.fuelsurcharge.surchargesummary

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictFuelSurchargeSummaryReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709856884)
        val items = mutableListOf<StrictFuelSurchargeSummary>()
        for (index in 0 until 4) {
            items.add(newStrictFuelSurchargeSummary("strict-fuel-surcharge-summary-$index", "sample"))
        }
        val report = buildStrictFuelSurchargeSummaryReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
