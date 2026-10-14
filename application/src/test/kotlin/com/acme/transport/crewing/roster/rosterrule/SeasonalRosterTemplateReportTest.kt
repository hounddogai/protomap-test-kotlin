package com.acme.transport.crewing.roster.rosterrule

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRosterTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708122454)
        val items = mutableListOf<SeasonalRosterTemplate>()
        for (index in 0 until 3) {
            items.add(newSeasonalRosterTemplate("seasonal-roster-template-$index", "sample"))
        }
        val report = buildSeasonalRosterTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
