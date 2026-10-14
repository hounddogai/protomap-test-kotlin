package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class ServiceAreaMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultServiceAreaMappingEngine()
        val item = newServiceAreaMapping("code-102", "name-686")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
