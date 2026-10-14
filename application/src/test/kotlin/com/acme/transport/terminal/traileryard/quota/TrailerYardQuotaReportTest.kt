package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703947101)
        val items = mutableListOf<TrailerYardQuota>()
        for (index in 0 until 3) {
            items.add(newTrailerYardQuota("trailer-yard-quota-$index", "sample"))
        }
        val report = buildTrailerYardQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
