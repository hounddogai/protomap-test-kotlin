package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertTrue

class TireMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTireMappingEngine()
        val item = newTireMapping("code-197", "name-223")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
