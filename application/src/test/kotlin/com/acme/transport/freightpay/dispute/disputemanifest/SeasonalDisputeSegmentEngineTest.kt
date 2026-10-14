package com.acme.transport.freightpay.dispute.disputemanifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalDisputeSegmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalDisputeSegmentEngine(newRegistry())
        val item = newSeasonalDisputeSegment("code-934", "title-965")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
