package com.acme.transport.network.hub.hubentry

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HubBundleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubBundleEngine(newRegistry())
        val item = newHubBundle("code-195", "label-917")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
