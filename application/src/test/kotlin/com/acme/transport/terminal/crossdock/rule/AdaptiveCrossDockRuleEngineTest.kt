package com.acme.transport.terminal.crossdock.rule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveCrossDockRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveCrossDockRuleEngine(newRegistry())
        val item = newAdaptiveCrossDockRule("code-38", "name-624")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
