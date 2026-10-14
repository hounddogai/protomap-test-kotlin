package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingAvailabilityMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingAvailabilityMappingEngine(newRegistry())
        val item = newRollingAvailabilityMapping("code-472", "name-182")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
