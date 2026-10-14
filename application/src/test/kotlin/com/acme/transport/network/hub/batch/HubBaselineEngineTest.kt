package com.acme.transport.network.hub.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HubBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubBaselineEngine(newRegistry())
        val item = newHubBaseline("code-758", "title-761")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
