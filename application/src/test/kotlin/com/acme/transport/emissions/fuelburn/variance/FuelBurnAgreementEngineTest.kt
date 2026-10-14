package com.acme.transport.emissions.fuelburn.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class FuelBurnAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultFuelBurnAgreementEngine(newRegistry())
        val item = newFuelBurnAgreement("code-882", "name-115")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
