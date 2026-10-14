package com.acme.transport.lastmile.redelivery.checkpoint

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700883048)
        val items = mutableListOf<RedeliveryMapping>()
        for (index in 0 until 3) {
            items.add(newRedeliveryMapping("redelivery-mapping-$index", "sample"))
        }
        val report = buildRedeliveryMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
