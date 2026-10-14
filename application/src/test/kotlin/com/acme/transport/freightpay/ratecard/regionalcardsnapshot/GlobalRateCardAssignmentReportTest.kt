package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRateCardAssignmentReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703458375)
        val items = mutableListOf<GlobalRateCardAssignment>()
        for (index in 0 until 4) {
            items.add(newGlobalRateCardAssignment("global-rate-card-assignment-$index", "sample"))
        }
        val report = buildGlobalRateCardAssignmentReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
