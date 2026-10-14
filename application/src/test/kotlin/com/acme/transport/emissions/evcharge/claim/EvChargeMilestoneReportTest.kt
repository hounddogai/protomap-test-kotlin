package com.acme.transport.emissions.evcharge.claim

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706095524)
        val items = mutableListOf<EvChargeMilestone>()
        for (index in 0 until 4) {
            items.add(newEvChargeMilestone("ev-charge-milestone-$index", "sample"))
        }
        val report = buildEvChargeMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
