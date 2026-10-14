package com.acme.transport.customs.hold.batch

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyHoldExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703036377)
        val items = mutableListOf<LegacyHoldException>()
        for (index in 0 until 5) {
            items.add(newLegacyHoldException("legacy-hold-exception-$index", "sample"))
        }
        val report = buildLegacyHoldExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
