package com.acme.transport.freightpay.ratecard.digest

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedRateCardScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700116034)
        val items = mutableListOf<CappedRateCardSchedule>()
        for (index in 0 until 3) {
            items.add(newCappedRateCardSchedule("capped-rate-card-schedule-$index", "sample"))
        }
        val report = buildCappedRateCardScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
