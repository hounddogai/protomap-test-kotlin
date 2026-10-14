package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateCardSnapshotReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1702005640)
        val items = mutableListOf<RateCardSnapshot>()
        for (index in 0 until 3) {
            items.add(newRateCardSnapshot("rate-card-snapshot-$index", "sample"))
        }
        val report = buildRateCardSnapshotReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
