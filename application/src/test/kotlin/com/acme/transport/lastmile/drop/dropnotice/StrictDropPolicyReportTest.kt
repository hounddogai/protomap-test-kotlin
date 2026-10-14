package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDropPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705066916)
        val items = mutableListOf<StrictDropPolicy>()
        for (index in 0 until 6) {
            items.add(newStrictDropPolicy("strict-drop-policy-$index", "sample"))
        }
        val report = buildStrictDropPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
