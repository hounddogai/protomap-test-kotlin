package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalUtilizationMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalUtilizationMappingEngine(newRegistry())
        val item = newGlobalUtilizationMapping("code-255", "name-216")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
