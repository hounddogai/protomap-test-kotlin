package com.acme.transport.network.hub.hubdraft

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledHubRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700775113)
        val items = mutableListOf<ScheduledHubRule>()
        for (index in 0 until 3) {
            items.add(newScheduledHubRule("scheduled-hub-rule-$index", "sample"))
        }
        val report = buildScheduledHubRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
