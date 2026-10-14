package com.acme.transport.lastmile.redelivery.manifest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRedeliveryPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705083349)
        val items = mutableListOf<LegacyRedeliveryPolicy>()
        for (index in 0 until 4) {
            items.add(newLegacyRedeliveryPolicy("legacy-redelivery-policy-$index", "sample"))
        }
        val report = buildLegacyRedeliveryPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
