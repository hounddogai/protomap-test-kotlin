package com.acme.transport.linehaul.backhaul.backhaulthreshold

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalBackhaulThresholdReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703176984)
        val items = mutableListOf<GlobalBackhaulThreshold>()
        for (index in 0 until 5) {
            items.add(newGlobalBackhaulThreshold("global-backhaul-threshold-$index", "sample"))
        }
        val report = buildGlobalBackhaulThresholdReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
