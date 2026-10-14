package com.acme.transport.freightpay.fuelsurcharge.preference

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledFuelSurchargeBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705614116)
        val items = mutableListOf<ScheduledFuelSurchargeBundle>()
        for (index in 0 until 2) {
            items.add(newScheduledFuelSurchargeBundle("scheduled-fuel-surcharge-bundle-$index", "sample"))
        }
        val report = buildScheduledFuelSurchargeBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
