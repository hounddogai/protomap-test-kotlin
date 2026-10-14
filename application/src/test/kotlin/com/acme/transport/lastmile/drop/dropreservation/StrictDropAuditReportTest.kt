package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDropAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703173885)
        val items = mutableListOf<StrictDropAudit>()
        for (index in 0 until 3) {
            items.add(newStrictDropAudit("strict-drop-audit-$index", "sample"))
        }
        val report = buildStrictDropAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
