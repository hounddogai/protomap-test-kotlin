package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorTemplateEngine(newRegistry())
        val item = newEmissionFactorTemplate("code-474", "label-622")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
