package com.acme.transport.network.node.template

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalNodeTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708667306)
        val items = mutableListOf<SeasonalNodeTemplate>()
        for (index in 0 until 5) {
            items.add(newSeasonalNodeTemplate("seasonal-node-template-$index", "sample"))
        }
        val report = buildSeasonalNodeTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
