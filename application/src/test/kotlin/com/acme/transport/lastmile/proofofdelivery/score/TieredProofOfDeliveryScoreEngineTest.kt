package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredProofOfDeliveryScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredProofOfDeliveryScoreEngine()
        val item = newTieredProofOfDeliveryScore("code-223", "name-11")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
