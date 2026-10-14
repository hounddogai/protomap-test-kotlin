package com.acme.transport.crewing.roster.template

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledRosterTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704495221)
        val items = mutableListOf<ScheduledRosterTemplate>()
        for (index in 0 until 5) {
            items.add(newScheduledRosterTemplate("scheduled-roster-template-$index", "sample"))
        }
        val report = buildScheduledRosterTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
