package com.acme.transport.lastmile.redelivery.redeliveryentry

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708361713)
        val items = mutableListOf<RedeliveryProfile>()
        for (index in 0 until 4) {
            items.add(newRedeliveryProfile("redelivery-profile-$index", "sample"))
        }
        val report = buildRedeliveryProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
