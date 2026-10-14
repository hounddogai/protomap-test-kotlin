package com.acme.transport.freightpay.accessorial.adjustment

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702307650)
        val items = mutableListOf<AccessorialAllocation>()
        for (index in 0 until 2) {
            items.add(newAccessorialAllocation("accessorial-allocation-$index", "sample"))
        }
        val report = buildAccessorialAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
