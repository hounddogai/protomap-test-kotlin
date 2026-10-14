package com.acme.transport.customs.broker.request

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackBrokerBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackBrokerBundleEngine(newRegistry())
        val item = newFallbackBrokerBundle("code-180", "name-432")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
