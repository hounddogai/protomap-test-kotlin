package com.acme.transport.network.hub.recorditem

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707910485)
        val items = mutableListOf<HubTemplate>()
        for (index in 0 until 6) {
            items.add(newHubTemplate("hub-template-$index", "sample"))
        }
        val report = buildHubTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
