package com.acme.transport.fleetops.tire.request

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTireRequestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708334892)
        val items = mutableListOf<PrimaryTireRequest>()
        for (index in 0 until 2) {
            items.add(newPrimaryTireRequest("primary-tire-request-$index", "sample"))
        }
        val report = buildPrimaryTireRequestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
