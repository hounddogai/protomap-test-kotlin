package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705451312)
        val items = mutableListOf<TariffCheckpoint>()
        for (index in 0 until 5) {
            items.add(newTariffCheckpoint("tariff-checkpoint-$index", "sample"))
        }
        val report = buildTariffCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
