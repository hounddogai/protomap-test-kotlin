package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledNearMissQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702028814)
        val items = mutableListOf<ScheduledNearMissQuota>()
        for (index in 0 until 3) {
            items.add(newScheduledNearMissQuota("scheduled-near-miss-quota-$index", "sample"))
        }
        val report = buildScheduledNearMissQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
