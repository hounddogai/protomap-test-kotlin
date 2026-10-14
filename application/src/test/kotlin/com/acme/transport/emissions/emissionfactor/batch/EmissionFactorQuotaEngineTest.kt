package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class EmissionFactorQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultEmissionFactorQuotaEngine(newRegistry())
        val item = newEmissionFactorQuota("code-696", "label-296")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
