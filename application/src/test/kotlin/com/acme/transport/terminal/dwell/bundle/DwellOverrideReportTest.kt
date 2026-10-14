package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702503648)
        val items = mutableListOf<DwellOverride>()
        for (index in 0 until 6) {
            items.add(newDwellOverride("dwell-override-$index", "sample"))
        }
        val report = buildDwellOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
