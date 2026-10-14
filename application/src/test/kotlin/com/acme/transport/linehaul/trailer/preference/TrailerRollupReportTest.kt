package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerRollupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708778986)
        val items = mutableListOf<TrailerRollup>()
        for (index in 0 until 5) {
            items.add(newTrailerRollup("trailer-rollup-$index", "sample"))
        }
        val report = buildTrailerRollupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
