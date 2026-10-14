package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HubExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubExceptionEngine(newRegistry())
        val item = newHubException("code-426", "label-997")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
