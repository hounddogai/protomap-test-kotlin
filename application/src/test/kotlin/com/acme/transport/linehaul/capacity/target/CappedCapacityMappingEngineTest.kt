package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedCapacityMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedCapacityMappingEngine()
        val item = newCappedCapacityMapping("code-311", "label-680")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
