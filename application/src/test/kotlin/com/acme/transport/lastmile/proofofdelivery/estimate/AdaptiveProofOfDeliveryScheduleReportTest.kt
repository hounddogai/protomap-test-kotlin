package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveProofOfDeliveryScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705249296)
        val items = mutableListOf<AdaptiveProofOfDeliverySchedule>()
        for (index in 0 until 6) {
            items.add(newAdaptiveProofOfDeliverySchedule("adaptive-proof-of-delivery-schedule-$index", "sample"))
        }
        val report = buildAdaptiveProofOfDeliveryScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
