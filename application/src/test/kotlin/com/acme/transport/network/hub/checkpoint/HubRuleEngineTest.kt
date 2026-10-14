package com.acme.transport.network.hub.checkpoint

import kotlin.test.Test
import kotlin.test.assertTrue

class HubRuleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubRuleEngine()
        val item = newHubRule("code-635", "title-465")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
