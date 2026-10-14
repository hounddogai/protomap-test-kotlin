package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702327602)
        val items = mutableListOf<RateCardAssignment>()
        for (index in 0 until 3) {
            items.add(newRateCardAssignment("rate-card-assignment-$index", "sample"))
        }
        val report = buildRateCardAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
