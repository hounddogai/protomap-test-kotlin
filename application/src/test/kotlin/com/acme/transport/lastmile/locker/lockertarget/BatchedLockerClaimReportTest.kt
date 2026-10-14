package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedLockerClaimReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702494939)
        val items = mutableListOf<BatchedLockerClaim>()
        for (index in 0 until 3) {
            items.add(newBatchedLockerClaim("batched-locker-claim-$index", "sample"))
        }
        val report = buildBatchedLockerClaimReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
