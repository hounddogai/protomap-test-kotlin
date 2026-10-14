package com.acme.transport.linehaul.capacity.agreement

import kotlin.test.Test
import kotlin.test.assertTrue

class SeasonalCapacityAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultSeasonalCapacityAgreementEngine()
        val item = newSeasonalCapacityAgreement("code-385", "name-926")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
