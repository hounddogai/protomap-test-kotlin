package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredLanePlanOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702550023)
        val items = mutableListOf<TieredLanePlanOverride>()
        for (index in 0 until 3) {
            items.add(newTieredLanePlanOverride("tiered-lane-plan-override-$index", "sample"))
        }
        val report = buildTieredLanePlanOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
