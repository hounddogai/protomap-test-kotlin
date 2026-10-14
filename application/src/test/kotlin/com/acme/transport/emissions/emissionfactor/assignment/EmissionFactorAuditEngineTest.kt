package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorAuditEngine()
        val item = newEmissionFactorAudit("code-645", "name-595")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
