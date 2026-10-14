package com.acme.transport.lastmile.proofofdelivery.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingProofOfDeliveryCycleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingProofOfDeliveryCycleEngine()
        val item = newRollingProofOfDeliveryCycle("code-862", "title-700")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
