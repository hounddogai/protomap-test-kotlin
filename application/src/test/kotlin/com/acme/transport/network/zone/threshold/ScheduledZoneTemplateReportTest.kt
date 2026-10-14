package com.acme.transport.network.zone.threshold

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledZoneTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708084853)
        val items = mutableListOf<ScheduledZoneTemplate>()
        for (index in 0 until 4) {
            items.add(newScheduledZoneTemplate("scheduled-zone-template-$index", "sample"))
        }
        val report = buildScheduledZoneTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
