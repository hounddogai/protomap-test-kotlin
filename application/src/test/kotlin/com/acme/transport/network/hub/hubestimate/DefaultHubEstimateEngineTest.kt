package com.acme.transport.network.hub.hubestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultHubEstimateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultHubEstimateEngine(newRegistry())
        val item = newDefaultHubEstimate("code-552", "label-828")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
