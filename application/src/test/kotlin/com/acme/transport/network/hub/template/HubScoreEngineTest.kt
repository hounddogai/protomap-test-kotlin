package com.acme.transport.network.hub.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HubScoreEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubScoreEngine(newRegistry())
        val item = newHubScore("code-937", "label-336")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
