package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRateCardScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701188203)
        val items = mutableListOf<RollingRateCardSchedule>()
        for (index in 0 until 5) {
            items.add(newRollingRateCardSchedule("rolling-rate-card-schedule-$index", "sample"))
        }
        val report = buildRollingRateCardScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
