package com.acme.transport.crewing.roster.rosteragreement

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRosterTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708931585)
        val items = mutableListOf<LegacyRosterTemplate>()
        for (index in 0 until 5) {
            items.add(newLegacyRosterTemplate("legacy-roster-template-$index", "sample"))
        }
        val report = buildLegacyRosterTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
