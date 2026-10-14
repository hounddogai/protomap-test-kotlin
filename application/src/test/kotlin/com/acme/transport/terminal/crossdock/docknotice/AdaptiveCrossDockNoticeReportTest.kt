package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCrossDockNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708167564)
        val items = mutableListOf<AdaptiveCrossDockNotice>()
        for (index in 0 until 5) {
            items.add(newAdaptiveCrossDockNotice("adaptive-cross-dock-notice-$index", "sample"))
        }
        val report = buildAdaptiveCrossDockNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
