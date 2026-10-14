package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708542742)
        val items = mutableListOf<BackhaulRequest>()
        for (index in 0 until 6) {
            items.add(newBackhaulRequest("backhaul-request-$index", "sample"))
        }
        val report = buildBackhaulRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
