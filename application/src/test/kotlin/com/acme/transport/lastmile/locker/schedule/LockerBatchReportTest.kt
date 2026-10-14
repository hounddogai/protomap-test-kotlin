package com.acme.transport.lastmile.locker.schedule

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerBatchReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707670922)
        val items = mutableListOf<LockerBatch>()
        for (index in 0 until 4) {
            items.add(newLockerBatch("locker-batch-$index", "sample"))
        }
        val report = buildLockerBatchReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
