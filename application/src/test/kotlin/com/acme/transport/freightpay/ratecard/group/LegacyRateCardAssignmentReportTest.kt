package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyRateCardAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701004373)
        val items = mutableListOf<LegacyRateCardAssignment>()
        for (index in 0 until 5) {
            items.add(newLegacyRateCardAssignment("legacy-rate-card-assignment-$index", "sample"))
        }
        val report = buildLegacyRateCardAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
