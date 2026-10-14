package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702529795)
        val items = mutableListOf<AccessorialBaseline>()
        for (index in 0 until 4) {
            items.add(newAccessorialBaseline("accessorial-baseline-$index", "sample"))
        }
        val report = buildAccessorialBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
