package com.acme.transport.fleetops.tire.window

import kotlin.test.Test
import kotlin.test.assertTrue

class TireRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireRequestEngine()
        val item = newTireRequest("code-984", "title-185")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
