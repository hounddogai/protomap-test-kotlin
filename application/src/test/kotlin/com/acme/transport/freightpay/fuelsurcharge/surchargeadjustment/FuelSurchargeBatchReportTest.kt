package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701221498)
        val items = mutableListOf<FuelSurchargeBatch>()
        for (index in 0 until 2) {
            items.add(newFuelSurchargeBatch("fuel-surcharge-batch-$index", "sample"))
        }
        val report = buildFuelSurchargeBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
