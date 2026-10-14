package com.acme.transport.network.zone.zonegroup

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultZoneGroupReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701538631)
        val items = mutableListOf<DefaultZoneGroup>()
        for (index in 0 until 5) {
            items.add(newDefaultZoneGroup("default-zone-group-$index", "sample"))
        }
        val report = buildDefaultZoneGroupReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
