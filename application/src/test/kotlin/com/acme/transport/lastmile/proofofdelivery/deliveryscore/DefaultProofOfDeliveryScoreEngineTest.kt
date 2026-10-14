package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultProofOfDeliveryScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultProofOfDeliveryScoreEngine(newRegistry())
        val item = newDefaultProofOfDeliveryScore("code-157", "title-545")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
