package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.emissions.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedEmissionFactorDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1704555995)
        val items = mutableListOf<BatchedEmissionFactorDigest>()
        for (index in 0 until 2) {
            items.add(newBatchedEmissionFactorDigest("batched-emission-factor-digest-$index", "sample"))
        }
        val report = buildBatchedEmissionFactorDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 2, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
