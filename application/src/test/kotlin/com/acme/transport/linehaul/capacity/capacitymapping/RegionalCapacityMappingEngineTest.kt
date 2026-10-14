package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalCapacityMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalCapacityMappingEngine()
        val item = newRegionalCapacityMapping("code-185", "label-354")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
