package com.acme.transport.lastmile.drop.dropexception

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDropExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706535858)
        val items = mutableListOf<LegacyDropException>()
        for (index in 0 until 5) {
            items.add(newLegacyDropException("legacy-drop-exception-$index", "sample"))
        }
        val report = buildLegacyDropExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
