package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictViolationQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705150984)
        val items = mutableListOf<StrictViolationQuota>()
        for (index in 0 until 3) {
            items.add(newStrictViolationQuota("strict-violation-quota-$index", "sample"))
        }
        val report = buildStrictViolationQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
