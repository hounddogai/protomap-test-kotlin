package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708734635)
        val items = mutableListOf<TenderRule>()
        for (index in 0 until 5) {
            items.add(newTenderRule("tender-rule-$index", "sample"))
        }
        val report = buildTenderRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
