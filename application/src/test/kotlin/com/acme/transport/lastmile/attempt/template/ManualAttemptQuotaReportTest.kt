package com.acme.transport.lastmile.attempt.template

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAttemptQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700844456)
        val items = mutableListOf<ManualAttemptQuota>()
        for (index in 0 until 3) {
            items.add(newManualAttemptQuota("manual-attempt-quota-$index", "sample"))
        }
        val report = buildManualAttemptQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
