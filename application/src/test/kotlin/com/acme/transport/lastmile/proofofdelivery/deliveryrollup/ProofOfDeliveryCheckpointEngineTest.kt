package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class ProofOfDeliveryCheckpointEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultProofOfDeliveryCheckpointEngine(newRegistry())
        val item = newProofOfDeliveryCheckpoint("code-64", "title-34")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
