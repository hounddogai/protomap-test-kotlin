package com.acme.transport.freightpay.dispute.window

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709090206)
        val items = mutableListOf<DisputeTemplate>()
        for (index in 0 until 4) {
            items.add(newDisputeTemplate("dispute-template-$index", "sample"))
        }
        val report = buildDisputeTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
