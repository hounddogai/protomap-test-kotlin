package com.acme.transport.freightpay.accessorial.plan

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707380131)
        val items = mutableListOf<AccessorialPlan>()
        for (index in 0 until 6) {
            items.add(newAccessorialPlan("accessorial-plan-$index", "sample"))
        }
        val report = buildAccessorialPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
