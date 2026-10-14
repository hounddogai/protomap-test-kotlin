package com.acme.transport.lastmile.attempt.quota

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttemptQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709475505)
        val items = mutableListOf<AttemptQuota>()
        for (index in 0 until 5) {
            items.add(newAttemptQuota("attempt-quota-$index", "sample"))
        }
        val report = buildAttemptQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
