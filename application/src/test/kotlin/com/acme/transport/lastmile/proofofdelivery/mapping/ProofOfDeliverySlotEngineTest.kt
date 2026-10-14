package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ProofOfDeliverySlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultProofOfDeliverySlotEngine(newRegistry())
        val item = newProofOfDeliverySlot("code-227", "label-34")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
