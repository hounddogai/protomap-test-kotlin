package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingNearMissMappingEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingNearMissMappingEngine()
        val item = newRollingNearMissMapping("code-471", "label-564")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
