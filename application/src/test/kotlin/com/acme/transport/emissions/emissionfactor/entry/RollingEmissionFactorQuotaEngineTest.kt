package com.acme.transport.emissions.emissionfactor.entry

import kotlin.test.Test
import kotlin.test.assertTrue

class RollingEmissionFactorQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingEmissionFactorQuotaEngine()
        val item = newRollingEmissionFactorQuota("code-775", "name-269")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
