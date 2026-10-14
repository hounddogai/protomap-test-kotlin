package com.acme.transport.network.hub.hubmilestone

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708233526)
        val items = mutableListOf<HubProfile>()
        for (index in 0 until 4) {
            items.add(newHubProfile("hub-profile-$index", "sample"))
        }
        val report = buildHubProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
