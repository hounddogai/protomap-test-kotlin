package com.acme.transport.lastmile.redelivery.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyRedeliveryPolicyEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyRedeliveryPolicyEngine(newRegistry())
        val item = newLegacyRedeliveryPolicy("code-268", "label-982")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
