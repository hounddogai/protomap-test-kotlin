package com.acme.transport.lastmile.locker.budget

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLockerAgreementReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709411474)
        val items = mutableListOf<ScheduledLockerAgreement>()
        for (index in 0 until 3) {
            items.add(newScheduledLockerAgreement("scheduled-locker-agreement-$index", "sample"))
        }
        val report = buildScheduledLockerAgreementReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
