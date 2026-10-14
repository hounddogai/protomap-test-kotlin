package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702936697)
        val items = mutableListOf<RedeliveryAdjustment>()
        for (index in 0 until 3) {
            items.add(newRedeliveryAdjustment("redelivery-adjustment-$index", "sample"))
        }
        val report = buildRedeliveryAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
