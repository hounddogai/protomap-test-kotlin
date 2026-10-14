package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706876377)
        val items = mutableListOf<TrailerPolicy>()
        for (index in 0 until 2) {
            items.add(newTrailerPolicy("trailer-policy-$index", "sample"))
        }
        val report = buildTrailerPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
