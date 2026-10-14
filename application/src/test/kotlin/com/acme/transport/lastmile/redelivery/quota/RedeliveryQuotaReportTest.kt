package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedeliveryQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706283888)
        val items = mutableListOf<RedeliveryQuota>()
        for (index in 0 until 2) {
            items.add(newRedeliveryQuota("redelivery-quota-$index", "sample"))
        }
        val report = buildRedeliveryQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
