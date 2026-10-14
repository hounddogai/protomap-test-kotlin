package com.acme.transport.linehaul.capacity.budget

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityManifestEngine()
        val item = newCapacityManifest("code-547", "title-514")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
