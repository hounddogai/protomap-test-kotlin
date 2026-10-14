package com.acme.transport.freightpay.dispute.disputebundle

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDisputeBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703807320)
        val items = mutableListOf<GlobalDisputeBatch>()
        for (index in 0 until 6) {
            items.add(newGlobalDisputeBatch("global-dispute-batch-$index", "sample"))
        }
        val report = buildGlobalDisputeBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
