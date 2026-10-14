package com.acme.transport.network.node.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalNodeMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalNodeMappingEngine()
        val item = newRegionalNodeMapping("code-160", "name-371")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
