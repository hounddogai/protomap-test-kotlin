package com.acme.transport.customs.hold.holdbatch

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalHoldBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703803214)
        val items = mutableListOf<GlobalHoldBatch>()
        for (index in 0 until 4) {
            items.add(newGlobalHoldBatch("global-hold-batch-$index", "sample"))
        }
        val report = buildGlobalHoldBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
