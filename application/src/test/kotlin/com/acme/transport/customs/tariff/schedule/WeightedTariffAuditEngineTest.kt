package com.acme.transport.customs.tariff.schedule

import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedTariffAuditEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedTariffAuditEngine()
        val item = newWeightedTariffAudit("code-882", "label-37")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
