package com.acme.transport.network.hub.target

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class HubTargetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultHubTargetEngine(newRegistry())
        val item = newHubTarget("code-63", "title-550")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
