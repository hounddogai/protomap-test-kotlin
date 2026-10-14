package com.acme.transport.fleetops.utilization.utilizationestimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class UtilizationRequestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultUtilizationRequestEngine(newRegistry())
        val item = newUtilizationRequest("code-931", "label-952")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
