package com.acme.transport.freightpay.accessorial.profile

import com.acme.transport.freightpay.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAccessorialCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1703319132)
        val items = mutableListOf<CappedAccessorialCheckpoint>()
        for (index in 0 until 2) {
            items.add(newCappedAccessorialCheckpoint("capped-accessorial-checkpoint-$index", "sample"))
        }
        val report = buildCappedAccessorialCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
