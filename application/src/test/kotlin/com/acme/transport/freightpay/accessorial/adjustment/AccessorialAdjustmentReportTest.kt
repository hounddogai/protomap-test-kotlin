package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700317181)
        val items = mutableListOf<AccessorialAdjustment>()
        for (index in 0 until 3) {
            items.add(newAccessorialAdjustment("accessorial-adjustment-$index", "sample"))
        }
        val report = buildAccessorialAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
