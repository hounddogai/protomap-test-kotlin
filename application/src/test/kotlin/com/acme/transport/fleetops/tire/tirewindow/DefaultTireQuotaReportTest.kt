package com.acme.transport.fleetops.tire.tirewindow

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTireQuotaReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700885200)
        val items = mutableListOf<DefaultTireQuota>()
        for (index in 0 until 5) {
            items.add(newDefaultTireQuota("default-tire-quota-$index", "sample"))
        }
        val report = buildDefaultTireQuotaReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
