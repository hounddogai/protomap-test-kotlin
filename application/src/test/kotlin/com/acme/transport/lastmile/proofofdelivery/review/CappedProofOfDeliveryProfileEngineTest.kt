package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedProofOfDeliveryProfileEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedProofOfDeliveryProfileEngine()
        val item = newCappedProofOfDeliveryProfile("code-762", "name-129")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
