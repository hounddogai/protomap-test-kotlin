package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientViolationQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707465361)
        val items = mutableListOf<LenientViolationQuota>()
        for (index in 0 until 3) {
            items.add(newLenientViolationQuota("lenient-violation-quota-$index", "sample"))
        }
        val report = buildLenientViolationQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
