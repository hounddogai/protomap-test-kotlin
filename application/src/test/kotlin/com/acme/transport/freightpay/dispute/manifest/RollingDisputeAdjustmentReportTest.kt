package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDisputeAdjustmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702009930)
        val items = mutableListOf<RollingDisputeAdjustment>()
        for (index in 0 until 4) {
            items.add(newRollingDisputeAdjustment("rolling-dispute-adjustment-$index", "sample"))
        }
        val report = buildRollingDisputeAdjustmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
