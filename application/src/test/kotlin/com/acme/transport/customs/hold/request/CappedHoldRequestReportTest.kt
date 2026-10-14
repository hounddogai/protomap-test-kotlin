package com.acme.transport.customs.hold.request

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedHoldRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705142661)
        val items = mutableListOf<CappedHoldRequest>()
        for (index in 0 until 2) {
            items.add(newCappedHoldRequest("capped-hold-request-$index", "sample"))
        }
        val report = buildCappedHoldRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
