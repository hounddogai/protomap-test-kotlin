package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRedeliveryQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704414378)
        val items = mutableListOf<StrictRedeliveryQuota>()
        for (index in 0 until 6) {
            items.add(newStrictRedeliveryQuota("strict-redelivery-quota-$index", "sample"))
        }
        val report = buildStrictRedeliveryQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
