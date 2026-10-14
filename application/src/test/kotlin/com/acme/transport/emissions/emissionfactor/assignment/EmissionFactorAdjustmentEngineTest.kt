package com.acme.transport.emissions.emissionfactor.assignment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorAdjustmentEngine(newRegistry())
        val item = newEmissionFactorAdjustment("code-707", "label-818")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
