package com.acme.transport.emissions.emissionfactor.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorExceptionEngine(newRegistry())
        val item = newEmissionFactorException("code-903", "label-785")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
