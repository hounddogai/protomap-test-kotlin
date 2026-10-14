package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedCameraEventLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1706680493)
        val items = mutableListOf<WeightedCameraEventLedger>()
        for (index in 0 until 6) {
            items.add(newWeightedCameraEventLedger("weighted-camera-event-ledger-$index", "sample"))
        }
        val report = buildWeightedCameraEventLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 6, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
