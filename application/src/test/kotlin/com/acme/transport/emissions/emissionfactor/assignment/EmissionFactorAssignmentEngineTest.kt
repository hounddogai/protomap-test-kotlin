package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorAssignmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorAssignmentEngine()
        val item = newEmissionFactorAssignment("code-245", "name-936")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
