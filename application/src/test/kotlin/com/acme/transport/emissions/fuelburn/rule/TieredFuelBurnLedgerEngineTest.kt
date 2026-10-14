package com.acme.transport.emissions.fuelburn.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class TieredFuelBurnLedgerEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTieredFuelBurnLedgerEngine()
        val item = newTieredFuelBurnLedger("code-154", "label-699")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
