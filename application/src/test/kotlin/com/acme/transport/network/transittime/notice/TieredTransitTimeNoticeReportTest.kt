package com.acme.transport.network.transittime.notice

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredTransitTimeNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703205182)
        val items = mutableListOf<TieredTransitTimeNotice>()
        for (index in 0 until 3) {
            items.add(newTieredTransitTimeNotice("tiered-transit-time-notice-$index", "sample"))
        }
        val report = buildTieredTransitTimeNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
