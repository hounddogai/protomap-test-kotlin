package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyHubSlotEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyHubSlotEngine()
        val item = newLegacyHubSlot("code-124", "label-956")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
