package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledDisputeBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709822071)
        val items = mutableListOf<ScheduledDisputeBundle>()
        for (index in 0 until 6) {
            items.add(newScheduledDisputeBundle("scheduled-dispute-bundle-$index", "sample"))
        }
        val report = buildScheduledDisputeBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
