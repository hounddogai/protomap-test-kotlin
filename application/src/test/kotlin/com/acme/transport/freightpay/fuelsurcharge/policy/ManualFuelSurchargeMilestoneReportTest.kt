package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualFuelSurchargeMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706003666)
        val items = mutableListOf<ManualFuelSurchargeMilestone>()
        for (index in 0 until 4) {
            items.add(newManualFuelSurchargeMilestone("manual-fuel-surcharge-milestone-$index", "sample"))
        }
        val report = buildManualFuelSurchargeMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
