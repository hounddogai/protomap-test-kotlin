package com.acme.transport.emissions.fuelburn.agreement

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LenientFuelBurnBudgetEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLenientFuelBurnBudgetEngine(newRegistry())
        val item = newLenientFuelBurnBudget("code-415", "name-429")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
