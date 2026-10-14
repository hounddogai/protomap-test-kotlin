package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultProofOfDeliveryManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultProofOfDeliveryManifestEngine(newRegistry())
        val item = newDefaultProofOfDeliveryManifest("code-659", "label-80")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
