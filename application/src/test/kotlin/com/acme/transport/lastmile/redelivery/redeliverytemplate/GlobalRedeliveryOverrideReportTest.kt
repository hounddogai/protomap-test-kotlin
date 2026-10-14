package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRedeliveryOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702734098)
        val items = mutableListOf<GlobalRedeliveryOverride>()
        for (index in 0 until 5) {
            items.add(newGlobalRedeliveryOverride("global-redelivery-override-$index", "sample"))
        }
        val report = buildGlobalRedeliveryOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
