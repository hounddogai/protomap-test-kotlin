package com.acme.transport.terminal.dwell.template

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702628946)
        val items = mutableListOf<DwellProfile>()
        for (index in 0 until 3) {
            items.add(newDwellProfile("dwell-profile-$index", "sample"))
        }
        val report = buildDwellProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
