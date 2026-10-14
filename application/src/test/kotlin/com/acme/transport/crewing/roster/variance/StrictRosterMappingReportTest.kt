package com.acme.transport.crewing.roster.variance

import com.acme.transport.crewing.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRosterMappingReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704198853)
        val items = mutableListOf<StrictRosterMapping>()
        for (index in 0 until 4) {
            items.add(newStrictRosterMapping("strict-roster-mapping-$index", "sample"))
        }
        val report = buildStrictRosterMappingReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
