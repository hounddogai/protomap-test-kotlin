package com.acme.transport.customs.tariff.preference

import kotlin.test.Test
import kotlin.test.assertTrue

class CappedTariffClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultCappedTariffClaimEngine()
        val item = newCappedTariffClaim("code-72", "name-676")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
