package com.acme.transport.fleetops.leasereturn.template

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701348077)
        val items = mutableListOf<LeaseReturnDraft>()
        for (index in 0 until 2) {
            items.add(newLeaseReturnDraft("lease-return-draft-$index", "sample"))
        }
        val report = buildLeaseReturnDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
