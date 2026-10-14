package com.acme.transport.terminal.crossdock.segment

import kotlin.test.Test
import kotlin.test.assertTrue

class CrossDockRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCrossDockRuleEngine()
        val item = newCrossDockRule("code-793", "name-475")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
