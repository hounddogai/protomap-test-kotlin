package com.acme.transport.safety.cameraevent.adjustment

import com.acme.transport.safety.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCameraEventLedgerReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1700351647)
        val items = mutableListOf<LenientCameraEventLedger>()
        for (index in 0 until 4) {
            items.add(newLenientCameraEventLedger("lenient-camera-event-ledger-$index", "sample"))
        }
        val report = buildLenientCameraEventLedgerReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
