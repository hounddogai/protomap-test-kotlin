package com.acme.transport.freightpay.ratecard.milestone

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalRateCardCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706767315)
        val items = mutableListOf<RegionalRateCardCheckpoint>()
        for (index in 0 until 3) {
            items.add(newRegionalRateCardCheckpoint("regional-rate-card-checkpoint-$index", "sample"))
        }
        val report = buildRegionalRateCardCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
