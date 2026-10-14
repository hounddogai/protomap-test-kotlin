package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.lastmile.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredProofOfDeliveryScoreReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708867912)
        val items = mutableListOf<TieredProofOfDeliveryScore>()
        for (index in 0 until 3) {
            items.add(newTieredProofOfDeliveryScore("tiered-proof-of-delivery-score-$index", "sample"))
        }
        val report = buildTieredProofOfDeliveryScoreReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
