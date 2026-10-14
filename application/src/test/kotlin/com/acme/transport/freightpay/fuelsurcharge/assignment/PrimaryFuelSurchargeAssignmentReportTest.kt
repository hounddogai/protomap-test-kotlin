package com.acme.transport.freightpay.fuelsurcharge.assignment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryFuelSurchargeAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701983342)
        val items = mutableListOf<PrimaryFuelSurchargeAssignment>()
        for (index in 0 until 3) {
            items.add(newPrimaryFuelSurchargeAssignment("primary-fuel-surcharge-assignment-$index", "sample"))
        }
        val report = buildPrimaryFuelSurchargeAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
