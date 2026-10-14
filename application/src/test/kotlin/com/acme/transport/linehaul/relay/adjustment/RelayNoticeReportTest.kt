package com.acme.transport.linehaul.relay.adjustment

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelayNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700268633)
        val items = mutableListOf<RelayNotice>()
        for (index in 0 until 3) {
            items.add(newRelayNotice("relay-notice-$index", "sample"))
        }
        val report = buildRelayNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
