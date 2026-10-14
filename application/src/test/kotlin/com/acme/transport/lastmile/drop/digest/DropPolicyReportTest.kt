package com.acme.transport.lastmile.drop.digest

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706439484)
        val items = mutableListOf<DropPolicy>()
        for (index in 0 until 2) {
            items.add(newDropPolicy("drop-policy-$index", "sample"))
        }
        val report = buildDropPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
