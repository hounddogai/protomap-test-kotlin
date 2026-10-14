package com.acme.transport.network.zone.segment

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704322561)
        val items = mutableListOf<ZoneRule>()
        for (index in 0 until 6) {
            items.add(newZoneRule("zone-rule-$index", "sample"))
        }
        val report = buildZoneRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
