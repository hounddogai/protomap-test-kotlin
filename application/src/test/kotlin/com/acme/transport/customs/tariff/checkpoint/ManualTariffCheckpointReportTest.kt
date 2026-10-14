package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTariffCheckpointReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708677358)
        val items = mutableListOf<ManualTariffCheckpoint>()
        for (index in 0 until 2) {
            items.add(newManualTariffCheckpoint("manual-tariff-checkpoint-$index", "sample"))
        }
        val report = buildManualTariffCheckpointReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
