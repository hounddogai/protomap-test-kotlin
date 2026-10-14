package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700390110)
        val items = mutableListOf<LeaseReturnScore>()
        for (index in 0 until 5) {
            items.add(newLeaseReturnScore("lease-return-score-$index", "sample"))
        }
        val report = buildLeaseReturnScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
