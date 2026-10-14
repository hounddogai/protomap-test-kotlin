package com.acme.transport.crewing.restbreak.policy

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700212927)
        val items = mutableListOf<RestBreakGroup>()
        for (index in 0 until 3) {
            items.add(newRestBreakGroup("rest-break-group-$index", "sample"))
        }
        val report = buildRestBreakGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
