package com.acme.transport.fleetops.tire.tier

import kotlin.test.Test
import kotlin.test.assertTrue

class TireTierEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireTierEngine()
        val item = newTireTier("code-10", "name-703")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
