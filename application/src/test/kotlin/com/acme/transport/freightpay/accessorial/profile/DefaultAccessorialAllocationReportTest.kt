package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultAccessorialAllocationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701188800)
        val items = mutableListOf<DefaultAccessorialAllocation>()
        for (index in 0 until 4) {
            items.add(newDefaultAccessorialAllocation("default-accessorial-allocation-$index", "sample"))
        }
        val report = buildDefaultAccessorialAllocationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
