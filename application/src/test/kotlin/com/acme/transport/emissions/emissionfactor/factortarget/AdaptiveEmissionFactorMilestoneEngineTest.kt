package com.acme.transport.emissions.emissionfactor.factortarget

import kotlin.test.Test
import kotlin.test.assertTrue

class AdaptiveEmissionFactorMilestoneEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultAdaptiveEmissionFactorMilestoneEngine()
        val item = newAdaptiveEmissionFactorMilestone("code-274", "title-743")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
