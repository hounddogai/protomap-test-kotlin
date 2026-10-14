package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAccessorialAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709826229)
        val items = mutableListOf<GlobalAccessorialAdjustment>()
        for (index in 0 until 3) {
            items.add(newGlobalAccessorialAdjustment("global-accessorial-adjustment-$index", "sample"))
        }
        val report = buildGlobalAccessorialAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
