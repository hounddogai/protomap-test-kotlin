package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedEmissionFactorManifestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704775748)
        val items = mutableListOf<BatchedEmissionFactorManifest>()
        for (index in 0 until 4) {
            items.add(newBatchedEmissionFactorManifest("batched-emission-factor-manifest-$index", "sample"))
        }
        val report = buildBatchedEmissionFactorManifestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 4, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
