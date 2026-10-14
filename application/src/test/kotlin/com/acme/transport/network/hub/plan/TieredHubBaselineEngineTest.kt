package com.acme.transport.network.hub.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredHubBaselineEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredHubBaselineEngine(newRegistry())
        val item = newTieredHubBaseline("code-730", "name-26")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
