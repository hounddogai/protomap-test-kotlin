package com.acme.transport.emissions.evcharge.chargequota

import kotlin.test.Test
import kotlin.test.assertTrue

class StrictEvChargeClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultStrictEvChargeClaimEngine()
        val item = newStrictEvChargeClaim("code-101", "name-177")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
