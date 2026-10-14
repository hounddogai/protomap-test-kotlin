package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredProofOfDeliveryManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredProofOfDeliveryManifestEngine()
        val item = newTieredProofOfDeliveryManifest("code-856", "name-734")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
