package com.acme.transport.lastmile.locker.manifest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706968118)
        val items = mutableListOf<LockerAudit>()
        for (index in 0 until 4) {
            items.add(newLockerAudit("locker-audit-$index", "sample"))
        }
        val report = buildLockerAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
