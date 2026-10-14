package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorBudgetEngine(newRegistry())
        val item = newEmissionFactorBudget("code-340", "name-671")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
