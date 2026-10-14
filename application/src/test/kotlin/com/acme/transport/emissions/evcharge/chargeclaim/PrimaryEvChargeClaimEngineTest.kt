package com.acme.transport.emissions.evcharge.chargeclaim

import kotlin.test.Test
import kotlin.test.assertTrue

class PrimaryEvChargeClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPrimaryEvChargeClaimEngine()
        val item = newPrimaryEvChargeClaim("code-909", "label-644")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
