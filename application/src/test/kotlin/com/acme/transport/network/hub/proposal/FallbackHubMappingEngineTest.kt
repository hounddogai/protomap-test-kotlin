package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackHubMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackHubMappingEngine(newRegistry())
        val item = newFallbackHubMapping("code-530", "title-436")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
