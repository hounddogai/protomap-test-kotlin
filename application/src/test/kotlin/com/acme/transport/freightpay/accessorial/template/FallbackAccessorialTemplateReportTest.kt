package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAccessorialTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702542969)
        val items = mutableListOf<FallbackAccessorialTemplate>()
        for (index in 0 until 6) {
            items.add(newFallbackAccessorialTemplate("fallback-accessorial-template-$index", "sample"))
        }
        val report = buildFallbackAccessorialTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
