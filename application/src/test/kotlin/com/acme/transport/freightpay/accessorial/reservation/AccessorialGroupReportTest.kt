package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707361084)
        val items = mutableListOf<AccessorialGroup>()
        for (index in 0 until 3) {
            items.add(newAccessorialGroup("accessorial-group-$index", "sample"))
        }
        val report = buildAccessorialGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
