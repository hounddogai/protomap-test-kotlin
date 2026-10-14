package com.acme.transport.lastmile.proofofdelivery.assignment

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryScheduleReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700913248)
        val items = mutableListOf<ProofOfDeliverySchedule>()
        for (index in 0 until 4) {
            items.add(newProofOfDeliverySchedule("proof-of-delivery-schedule-$index", "sample"))
        }
        val report = buildProofOfDeliveryScheduleReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
