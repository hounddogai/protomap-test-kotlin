package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class TieredEmissionFactorExceptionEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredEmissionFactorExceptionEngine(newRegistry())
        val item = newTieredEmissionFactorException("code-997", "title-843")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
