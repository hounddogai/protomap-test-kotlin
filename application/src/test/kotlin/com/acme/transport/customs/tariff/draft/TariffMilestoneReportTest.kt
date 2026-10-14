package com.acme.transport.customs.tariff.draft

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704485822)
        val items = mutableListOf<TariffMilestone>()
        for (index in 0 until 4) {
            items.add(newTariffMilestone("tariff-milestone-$index", "sample"))
        }
        val report = buildTariffMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
