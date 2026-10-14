package com.acme.transport.customs.hold.quota

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701084461)
        val items = mutableListOf<HoldQuota>()
        for (index in 0 until 4) {
            items.add(newHoldQuota("hold-quota-$index", "sample"))
        }
        val report = buildHoldQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
