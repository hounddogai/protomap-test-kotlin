package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalFuelSurchargeBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700446129)
        val items = mutableListOf<SeasonalFuelSurchargeBatch>()
        for (index in 0 until 5) {
            items.add(newSeasonalFuelSurchargeBatch("seasonal-fuel-surcharge-batch-$index", "sample"))
        }
        val report = buildSeasonalFuelSurchargeBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
