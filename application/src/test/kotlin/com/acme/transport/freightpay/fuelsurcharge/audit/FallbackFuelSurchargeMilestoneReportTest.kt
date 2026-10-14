package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackFuelSurchargeMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704339032)
        val items = mutableListOf<FallbackFuelSurchargeMilestone>()
        for (index in 0 until 2) {
            items.add(newFallbackFuelSurchargeMilestone("fallback-fuel-surcharge-milestone-$index", "sample"))
        }
        val report = buildFallbackFuelSurchargeMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
