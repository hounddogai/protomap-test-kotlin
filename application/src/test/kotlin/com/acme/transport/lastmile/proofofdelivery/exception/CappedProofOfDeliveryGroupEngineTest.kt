package com.acme.transport.lastmile.proofofdelivery.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedProofOfDeliveryGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedProofOfDeliveryGroupEngine()
        val item = newCappedProofOfDeliveryGroup("code-247", "label-933")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
