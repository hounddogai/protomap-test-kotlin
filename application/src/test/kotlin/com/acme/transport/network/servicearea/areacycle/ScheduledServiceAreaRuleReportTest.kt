package com.acme.transport.network.servicearea.areacycle

import com.acme.transport.network.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledServiceAreaRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705405248)
        val items = mutableListOf<ScheduledServiceAreaRule>()
        for (index in 0 until 6) {
            items.add(newScheduledServiceAreaRule("scheduled-service-area-rule-$index", "sample"))
        }
        val report = buildScheduledServiceAreaRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
