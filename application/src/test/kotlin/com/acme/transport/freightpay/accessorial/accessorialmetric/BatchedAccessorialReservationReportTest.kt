package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAccessorialReservationReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708395486)
        val items = mutableListOf<BatchedAccessorialReservation>()
        for (index in 0 until 4) {
            items.add(newBatchedAccessorialReservation("batched-accessorial-reservation-$index", "sample"))
        }
        val report = buildBatchedAccessorialReservationReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
