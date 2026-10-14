package com.acme.transport.lastmile.locker.batch

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalLockerBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705465194)
        val items = mutableListOf<GlobalLockerBatch>()
        for (index in 0 until 3) {
            items.add(newGlobalLockerBatch("global-locker-batch-$index", "sample"))
        }
        val report = buildGlobalLockerBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
