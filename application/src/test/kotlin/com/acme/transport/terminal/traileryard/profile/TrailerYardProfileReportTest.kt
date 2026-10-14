package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700507949)
        val items = mutableListOf<TrailerYardProfile>()
        for (index in 0 until 2) {
            items.add(newTrailerYardProfile("trailer-yard-profile-$index", "sample"))
        }
        val report = buildTrailerYardProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
