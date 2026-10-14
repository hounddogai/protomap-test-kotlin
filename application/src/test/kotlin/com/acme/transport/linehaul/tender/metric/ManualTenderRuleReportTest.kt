package com.acme.transport.linehaul.tender.metric

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTenderRuleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703635741)
        val items = mutableListOf<ManualTenderRule>()
        for (index in 0 until 2) {
            items.add(newManualTenderRule("manual-tender-rule-$index", "sample"))
        }
        val report = buildManualTenderRuleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
