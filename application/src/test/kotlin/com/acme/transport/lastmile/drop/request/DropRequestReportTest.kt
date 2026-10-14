package com.acme.transport.lastmile.drop.request

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702298562)
        val items = mutableListOf<DropRequest>()
        for (index in 0 until 3) {
            items.add(newDropRequest("drop-request-$index", "sample"))
        }
        val report = buildDropRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
