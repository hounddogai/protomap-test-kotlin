package com.acme.transport.network.servicearea.areaexception

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedServiceAreaMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedServiceAreaMappingEngine()
        val item = newCappedServiceAreaMapping("code-504", "label-356")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
