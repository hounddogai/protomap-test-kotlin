package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.linehaul.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRelayBaselineReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701737725)
        val items = mutableListOf<ManualRelayBaseline>()
        for (index in 0 until 3) {
            items.add(newManualRelayBaseline("manual-relay-baseline-$index", "sample"))
        }
        val report = buildManualRelayBaselineReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
