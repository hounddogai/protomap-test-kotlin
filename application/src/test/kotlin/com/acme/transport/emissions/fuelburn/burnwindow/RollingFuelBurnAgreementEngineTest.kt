package com.acme.transport.emissions.fuelburn.burnwindow

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class RollingFuelBurnAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRollingFuelBurnAgreementEngine(newRegistry())
        val item = newRollingFuelBurnAgreement("code-557", "label-853")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
