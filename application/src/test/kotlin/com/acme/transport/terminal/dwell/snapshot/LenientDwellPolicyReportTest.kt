package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.terminal.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDwellPolicyReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706367153)
        val items = mutableListOf<LenientDwellPolicy>()
        for (index in 0 until 6) {
            items.add(newLenientDwellPolicy("lenient-dwell-policy-$index", "sample"))
        }
        val report = buildLenientDwellPolicyReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
