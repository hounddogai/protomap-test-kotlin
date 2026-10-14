package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveDisputeManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveDisputeManifestEngine(newRegistry())
        val item = newAdaptiveDisputeManifest("code-71", "name-458")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
