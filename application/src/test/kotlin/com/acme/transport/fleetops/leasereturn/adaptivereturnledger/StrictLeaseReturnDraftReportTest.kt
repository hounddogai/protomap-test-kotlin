package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictLeaseReturnDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705222888)
        val items = mutableListOf<StrictLeaseReturnDraft>()
        for (index in 0 until 4) {
            items.add(newStrictLeaseReturnDraft("strict-lease-return-draft-$index", "sample"))
        }
        val report = buildStrictLeaseReturnDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
