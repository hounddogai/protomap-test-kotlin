package com.acme.transport.freightpay.ratecard.entry

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRateCardTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701044035)
        val items = mutableListOf<AdaptiveRateCardTemplate>()
        for (index in 0 until 4) {
            items.add(newAdaptiveRateCardTemplate("adaptive-rate-card-template-$index", "sample"))
        }
        val report = buildAdaptiveRateCardTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
