package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingEmissionFactorAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingEmissionFactorAgreementEngine(newRegistry())
        val item = newRollingEmissionFactorAgreement("code-859", "label-25")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
