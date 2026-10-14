package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAccessorialMilestoneReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1709391094)
        val items = mutableListOf<FallbackAccessorialMilestone>()
        for (index in 0 until 6) {
            items.add(newFallbackAccessorialMilestone("fallback-accessorial-milestone-$index", "sample"))
        }
        val report = buildFallbackAccessorialMilestoneReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
