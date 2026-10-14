package com.acme.transport.network.hub.recorditem

import kotlin.test.Test
import kotlin.test.assertTrue

class HubTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubTemplateEngine()
        val item = newHubTemplate("code-287", "title-351")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
