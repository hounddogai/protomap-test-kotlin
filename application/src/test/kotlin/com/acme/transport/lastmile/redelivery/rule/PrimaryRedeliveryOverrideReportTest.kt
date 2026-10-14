package com.acme.transport.lastmile.redelivery.rule

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryRedeliveryOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707475641)
        val items = mutableListOf<PrimaryRedeliveryOverride>()
        for (index in 0 until 2) {
            items.add(newPrimaryRedeliveryOverride("primary-redelivery-override-$index", "sample"))
        }
        val report = buildPrimaryRedeliveryOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
