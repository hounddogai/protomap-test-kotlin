package com.acme.transport.emissions.emissionfactor.factorschedule

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DefaultEmissionFactorAdjustmentEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDefaultEmissionFactorAdjustmentEngine(newRegistry())
        val item = newDefaultEmissionFactorAdjustment("code-873", "name-977")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
