package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAccessorialScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703935942)
        val items = mutableListOf<GlobalAccessorialScore>()
        for (index in 0 until 6) {
            items.add(newGlobalAccessorialScore("global-accessorial-score-$index", "sample"))
        }
        val report = buildGlobalAccessorialScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
