package com.acme.transport.crewing.availability.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveAvailabilityMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveAvailabilityMappingEngine(newRegistry())
        val item = newAdaptiveAvailabilityMapping("code-373", "title-655")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
