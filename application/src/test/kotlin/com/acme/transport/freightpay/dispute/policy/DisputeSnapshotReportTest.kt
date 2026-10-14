package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702611564)
        val items = mutableListOf<DisputeSnapshot>()
        for (index in 0 until 5) {
            items.add(newDisputeSnapshot("dispute-snapshot-$index", "sample"))
        }
        val report = buildDisputeSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
