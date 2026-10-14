package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedLanePlanPreferenceReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709874466)
        val items = mutableListOf<CappedLanePlanPreference>()
        for (index in 0 until 3) {
            items.add(newCappedLanePlanPreference("capped-lane-plan-preference-$index", "sample"))
        }
        val report = buildCappedLanePlanPreferenceReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
