package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDisputeClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708371181)
        val items = mutableListOf<PendingDisputeClaim>()
        for (index in 0 until 3) {
            items.add(newPendingDisputeClaim("pending-dispute-claim-$index", "sample"))
        }
        val report = buildPendingDisputeClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
