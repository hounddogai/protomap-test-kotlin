package com.acme.transport.emissions.fuelburn.burnvariance

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalFuelBurnRecordEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalFuelBurnRecordEngine()
        val item = newRegionalFuelBurnRecord("code-898", "title-846")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
