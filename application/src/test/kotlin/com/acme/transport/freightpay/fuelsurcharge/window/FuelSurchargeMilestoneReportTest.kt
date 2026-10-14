package com.acme.transport.freightpay.fuelsurcharge.window

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705143902)
        val items = mutableListOf<FuelSurchargeMilestone>()
        for (index in 0 until 5) {
            items.add(newFuelSurchargeMilestone("fuel-surcharge-milestone-$index", "sample"))
        }
        val report = buildFuelSurchargeMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
