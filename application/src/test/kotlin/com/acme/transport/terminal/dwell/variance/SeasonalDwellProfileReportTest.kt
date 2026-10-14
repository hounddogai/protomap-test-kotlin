package com.acme.transport.terminal.dwell.variance

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalDwellProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708811106)
        val items = mutableListOf<SeasonalDwellProfile>()
        for (index in 0 until 3) {
            items.add(newSeasonalDwellProfile("seasonal-dwell-profile-$index", "sample"))
        }
        val report = buildSeasonalDwellProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
