package com.acme.transport.customs.tariff.claim

import kotlin.test.Test
import kotlin.test.assertTrue

class TariffClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultTariffClaimEngine()
        val item = newTariffClaim("code-788", "label-435")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
