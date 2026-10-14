package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HubOverrideEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubOverrideEngine(newRegistry())
        val item = newHubOverride("code-972", "title-921")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
