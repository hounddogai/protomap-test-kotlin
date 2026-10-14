package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAccessorialScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707487258)
        val items = mutableListOf<FallbackAccessorialScore>()
        for (index in 0 until 2) {
            items.add(newFallbackAccessorialScore("fallback-accessorial-score-$index", "sample"))
        }
        val report = buildFallbackAccessorialScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
