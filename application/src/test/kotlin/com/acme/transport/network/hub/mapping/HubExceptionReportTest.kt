package com.acme.transport.network.hub.mapping

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubExceptionReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707520909)
        val items = mutableListOf<HubException>()
        for (index in 0 until 6) {
            items.add(newHubException("hub-exception-$index", "sample"))
        }
        val report = buildHubExceptionReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
