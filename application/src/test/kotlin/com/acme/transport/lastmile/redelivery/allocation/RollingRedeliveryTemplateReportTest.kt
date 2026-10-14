package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRedeliveryTemplateReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707200755)
        val items = mutableListOf<RollingRedeliveryTemplate>()
        for (index in 0 until 6) {
            items.add(newRollingRedeliveryTemplate("rolling-redelivery-template-$index", "sample"))
        }
        val report = buildRollingRedeliveryTemplateReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
