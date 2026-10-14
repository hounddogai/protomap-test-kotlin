package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HubWindowEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubWindowEngine(newRegistry())
        val item = newHubWindow("code-942", "name-706")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
