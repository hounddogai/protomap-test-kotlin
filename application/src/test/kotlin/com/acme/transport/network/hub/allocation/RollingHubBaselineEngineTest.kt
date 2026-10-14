package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingHubBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingHubBaselineEngine()
        val item = newRollingHubBaseline("code-958", "label-96")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
