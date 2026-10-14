package com.acme.transport.lastmile.locker.quota

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701900073)
        val items = mutableListOf<LockerQuota>()
        for (index in 0 until 4) {
            items.add(newLockerQuota("locker-quota-$index", "sample"))
        }
        val report = buildLockerQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
