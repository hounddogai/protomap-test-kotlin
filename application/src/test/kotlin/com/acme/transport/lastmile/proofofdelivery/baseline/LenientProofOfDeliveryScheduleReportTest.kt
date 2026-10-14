package com.acme.transport.lastmile.proofofdelivery.baseline

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientProofOfDeliveryScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704218870)
        val items = mutableListOf<LenientProofOfDeliverySchedule>()
        for (index in 0 until 6) {
            items.add(newLenientProofOfDeliverySchedule("lenient-proof-of-delivery-schedule-$index", "sample"))
        }
        val report = buildLenientProofOfDeliveryScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
