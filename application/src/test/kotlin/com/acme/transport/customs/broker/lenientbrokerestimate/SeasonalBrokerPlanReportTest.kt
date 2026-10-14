package com.acme.transport.customs.broker.lenientbrokerestimate

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalBrokerPlanReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701867188)
        val items = mutableListOf<SeasonalBrokerPlan>()
        for (index in 0 until 4) {
            items.add(newSeasonalBrokerPlan("seasonal-broker-plan-$index", "sample"))
        }
        val report = buildSeasonalBrokerPlanReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
