package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.fleetops.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedUtilizationPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708383223)
        val items = mutableListOf<CappedUtilizationPreference>()
        for (index in 0 until 2) {
            items.add(newCappedUtilizationPreference("capped-utilization-preference-$index", "sample"))
        }
        val report = buildCappedUtilizationPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
