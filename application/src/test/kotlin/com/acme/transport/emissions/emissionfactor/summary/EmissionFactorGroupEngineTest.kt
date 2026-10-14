package com.acme.transport.emissions.emissionfactor.summary

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorGroupEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorGroupEngine(newRegistry())
        val item = newEmissionFactorGroup("code-590", "label-810")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
