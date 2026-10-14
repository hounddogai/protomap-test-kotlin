package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedHubMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedHubMappingEngine()
        val item = newCappedHubMapping("code-49", "name-225")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
