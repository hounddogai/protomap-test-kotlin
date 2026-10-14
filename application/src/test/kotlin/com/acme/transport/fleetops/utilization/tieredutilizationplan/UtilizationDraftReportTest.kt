package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationDraftReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703329040)
        val items = mutableListOf<UtilizationDraft>()
        for (index in 0 until 5) {
            items.add(newUtilizationDraft("utilization-draft-$index", "sample"))
        }
        val report = buildUtilizationDraftReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
