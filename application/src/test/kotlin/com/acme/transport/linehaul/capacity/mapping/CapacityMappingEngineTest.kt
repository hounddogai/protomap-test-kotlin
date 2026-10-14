package com.acme.transport.linehaul.capacity.mapping

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCapacityMappingEngine()
        val item = newCapacityMapping("code-478", "title-988")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
