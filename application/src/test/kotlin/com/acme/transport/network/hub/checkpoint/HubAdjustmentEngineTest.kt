package com.acme.transport.network.hub.checkpoint

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HubAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubAdjustmentEngine(newRegistry())
        val item = newHubAdjustment("code-615", "name-217")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
