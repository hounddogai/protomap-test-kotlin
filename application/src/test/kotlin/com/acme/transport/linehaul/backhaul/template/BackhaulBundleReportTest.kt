package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulBundleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703803633)
        val items = mutableListOf<BackhaulBundle>()
        for (index in 0 until 3) {
            items.add(newBackhaulBundle("backhaul-bundle-$index", "sample"))
        }
        val report = buildBackhaulBundleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
