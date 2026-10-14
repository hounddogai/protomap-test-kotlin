package com.acme.transport.emissions.emissionfactor.template

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedEmissionFactorTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedEmissionFactorTemplateEngine(newRegistry())
        val item = newWeightedEmissionFactorTemplate("code-867", "name-76")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
