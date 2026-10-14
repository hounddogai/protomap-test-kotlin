package com.acme.transport.emissions.emissionfactor.quota

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FallbackEmissionFactorQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFallbackEmissionFactorQuotaEngine(newRegistry())
        val item = newFallbackEmissionFactorQuota("code-917", "name-596")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
