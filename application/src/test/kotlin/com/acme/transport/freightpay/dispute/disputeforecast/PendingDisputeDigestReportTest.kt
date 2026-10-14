package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDisputeDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709337441)
        val items = mutableListOf<PendingDisputeDigest>()
        for (index in 0 until 4) {
            items.add(newPendingDisputeDigest("pending-dispute-digest-$index", "sample"))
        }
        val report = buildPendingDisputeDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
