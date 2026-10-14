package com.acme.transport.emissions.fuelburn.burnvariance

import kotlin.test.Test
import kotlin.test.assertTrue

class GlobalFuelBurnAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultGlobalFuelBurnAssignmentEngine()
        val item = newGlobalFuelBurnAssignment("code-822", "name-881")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
